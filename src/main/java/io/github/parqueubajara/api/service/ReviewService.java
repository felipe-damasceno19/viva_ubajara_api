package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.request.ReviewRequestDTO;
import io.github.parqueubajara.api.dto.response.ReviewAdminResponseDTO;
import io.github.parqueubajara.api.dto.response.ReviewResponseDTO;
import io.github.parqueubajara.api.dto.response.ReviewSummaryDTO;
import io.github.parqueubajara.api.exception.ConflictException;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.model.Attraction;
import io.github.parqueubajara.api.model.HostPoint;
import io.github.parqueubajara.api.model.Restaurant;
import io.github.parqueubajara.api.model.Review;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.TouristSpot;
import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import io.github.parqueubajara.api.repository.ReviewRepository;
import io.github.parqueubajara.api.repository.TouristSpotRepository;
import io.github.parqueubajara.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int MIN_COMMENT_LENGTH = 10;

    private final ReviewRepository reviewRepository;
    private final TouristSpotRepository touristSpotRepository;
    private final UserRepository userRepository;

    public record RatingSummary(Double average, long count) {
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> listApproved(UUID spotId, Pageable pageable) {
        return reviewRepository.findBySpotIdAndStatus(spotId, ApprovalStatus.APPROVED, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryDTO summary(UUID spotId) {
        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int stars = 1; stars <= 5; stars++) {
            distribution.put(stars, 0L);
        }
        long count = 0;
        long total = 0;
        for (Object[] row : reviewRepository.distributionBySpotId(spotId)) {
            int stars = ((Number) row[0]).intValue();
            long amount = ((Number) row[1]).longValue();
            distribution.put(stars, amount);
            count += amount;
            total += stars * amount;
        }
        return new ReviewSummaryDTO(count == 0 ? null : round(total / (double) count), count, distribution);
    }

    @Transactional(readOnly = true)
    public Optional<ReviewResponseDTO> findMine(SystemUser user, UUID spotId) {
        return reviewRepository.findBySpotIdAndAuthorId(spotId, user.getId()).map(this::toResponseDTO);
    }

    @Transactional
    public ReviewResponseDTO upsert(SystemUser user, UUID spotId, ReviewRequestDTO dto) {
        SystemUser author = userRepository.findByIdForUpdate(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        TouristSpot spot = touristSpotRepository.findById(spotId)
                .filter(s -> Boolean.TRUE.equals(s.getActive()) && s.getApprovalStatus() == ApprovalStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Local não encontrado"));

        if (spot.getOwner() != null && spot.getOwner().getId().equals(author.getId())) {
            throw new ConflictException("Você não pode avaliar o seu próprio estabelecimento");
        }

        String comment = normalizeComment(dto.comment());

        Review review = reviewRepository.findBySpotIdAndAuthorId(spotId, author.getId()).orElseGet(() -> {
            Review created = new Review();
            created.setSpot(spot);
            created.setAuthor(author);
            return created;
        });
        review.setRating(dto.rating());
        review.setComment(comment);
        review.setStatus(ApprovalStatus.PENDING);

        try {
            reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Não foi possível salvar sua avaliação. Tente novamente");
        }
        return toResponseDTO(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewAdminResponseDTO> listForModeration(ApprovalStatus status, Pageable pageable) {
        Page<Review> page = status == null
                ? reviewRepository.findAll(pageable)
                : reviewRepository.findByStatus(status, pageable);
        return page.map(this::toAdminResponseDTO);
    }

    @Transactional
    public void updateStatus(UUID id, ApprovalStatus status) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada"));
        review.setStatus(status);
        reviewRepository.save(review);
    }

    @Transactional
    public void delete(UUID id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Avaliação não encontrada");
        }
        reviewRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<UUID, RatingSummary> summariesFor(Collection<UUID> spotIds) {
        if (spotIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, RatingSummary> result = new HashMap<>();
        for (Object[] row : reviewRepository.summarizeBySpotIds(spotIds)) {
            result.put((UUID) row[0], new RatingSummary(round(((Number) row[1]).doubleValue()), ((Number) row[2]).longValue()));
        }
        return result;
    }

    private String normalizeComment(String raw) {
        String trimmed = raw == null ? null : raw.trim();
        if (trimmed == null || trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() < MIN_COMMENT_LENGTH) {
            throw new IllegalArgumentException("O comentário deve ter pelo menos " + MIN_COMMENT_LENGTH + " caracteres");
        }
        return trimmed;
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private ReviewResponseDTO toResponseDTO(Review review) {
        SystemUser author = review.getAuthor();
        return new ReviewResponseDTO(
                review.getId(), review.getRating(), review.getComment(),
                publicName(author), author.getPhotoUrl(), review.getStatus(), review.getCreatedDate()
        );
    }

    private ReviewAdminResponseDTO toAdminResponseDTO(Review review) {
        TouristSpot spot = review.getSpot();
        SystemUser author = review.getAuthor();
        return new ReviewAdminResponseDTO(
                review.getId(), review.getRating(), review.getComment(), review.getStatus(), review.getCreatedDate(),
                new ReviewAdminResponseDTO.Spot(spot.getId(), spot.getName(), typeOf(spot)),
                new ReviewAdminResponseDTO.Author(author.getId(), fullName(author), author.getEmail())
        );
    }

    private String typeOf(TouristSpot spot) {
        Object real = Hibernate.unproxy(spot);
        if (real instanceof Restaurant) return "RESTAURANT";
        if (real instanceof HostPoint) return "HOST_POINT";
        if (real instanceof Attraction) return "ATTRACTION";
        return "SPOT";
    }

    private String publicName(SystemUser user) {
        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();
        if (last.isEmpty()) {
            return first;
        }
        return first + " " + last.substring(0, last.offsetByCodePoints(0, 1)) + ".";
    }

    private String fullName(SystemUser user) {
        return user.getLastName() == null || user.getLastName().isBlank()
                ? user.getFirstName()
                : user.getFirstName() + " " + user.getLastName();
    }
}
