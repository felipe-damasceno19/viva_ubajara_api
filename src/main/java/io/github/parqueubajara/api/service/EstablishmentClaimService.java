package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.request.EstablishmentClaimRequestDTO;
import io.github.parqueubajara.api.dto.response.EstablishmentClaimAdminResponseDTO;
import io.github.parqueubajara.api.dto.response.EstablishmentClaimResponseDTO;
import io.github.parqueubajara.api.exception.ConflictException;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.exception.TooManyRequestsException;
import io.github.parqueubajara.api.model.EstablishmentClaim;
import io.github.parqueubajara.api.model.HostPoint;
import io.github.parqueubajara.api.model.Restaurant;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.TouristSpot;
import io.github.parqueubajara.api.model.enums.ClaimStatus;
import io.github.parqueubajara.api.model.enums.EstablishmentType;
import io.github.parqueubajara.api.model.enums.Role;
import io.github.parqueubajara.api.repository.EstablishmentClaimRepository;
import io.github.parqueubajara.api.repository.HostPointRepository;
import io.github.parqueubajara.api.repository.RestaurantRepository;
import io.github.parqueubajara.api.repository.TouristSpotRepository;
import io.github.parqueubajara.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstablishmentClaimService {

    private static final int MAX_CLAIMS_PER_DAY = 5;
    private static final String AUTO_REJECTION_REASON = "Outra solicitação foi aprovada para este estabelecimento.";

    private final EstablishmentClaimRepository claimRepository;
    private final TouristSpotRepository touristSpotRepository;
    private final RestaurantRepository restaurantRepository;
    private final HostPointRepository hostPointRepository;
    private final UserRepository userRepository;

    @Transactional
    public EstablishmentClaimResponseDTO create(SystemUser user, EstablishmentClaimRequestDTO dto) {
        SystemUser claimant = userRepository.findByIdForUpdate(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (claimant.getUserRole() != Role.EMPRESA) {
            throw new AccessDeniedException("Apenas contas empresariais podem reivindicar estabelecimentos");
        }

        TouristSpot spot = touristSpotRepository.findById(dto.spotId())
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado"));

        if (!isClaimableType(spot)) {
            throw new IllegalArgumentException("Este local não pode ser reivindicado");
        }

        if (!spot.isClaimable()) {
            throw new ConflictException("Este estabelecimento não está disponível para reivindicação");
        }

        if (hasEstablishment(claimant.getId())) {
            throw new ConflictException("Sua conta já possui um estabelecimento vinculado");
        }

        if (claimRepository.existsByClaimantIdAndStatus(claimant.getId(), ClaimStatus.PENDING)) {
            throw new ConflictException("Você já tem uma reivindicação em análise. Cancele-a para enviar outra");
        }

        if (claimRepository.countByClaimantIdAndCreatedDateAfter(claimant.getId(), LocalDateTime.now().minusHours(24))
                >= MAX_CLAIMS_PER_DAY) {
            throw new TooManyRequestsException("Limite de solicitações atingido. Tente novamente amanhã");
        }

        EstablishmentClaim claim = new EstablishmentClaim();
        claim.setSpot(spot);
        claim.setClaimant(claimant);
        claim.setMessage(dto.message().trim());
        claim.setContactPhone(dto.contactPhone().trim());
        claim.setStatus(ClaimStatus.PENDING);
        claimRepository.save(claim);
        return toResponseDTO(claim);
    }

    @Transactional(readOnly = true)
    public List<EstablishmentClaimResponseDTO> listMine(SystemUser user) {
        return claimRepository.findByClaimantIdOrderByCreatedDateDesc(user.getId()).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public void cancel(SystemUser user, UUID claimId) {
        EstablishmentClaim claim = claimRepository.findByIdForUpdate(claimId)
                .filter(c -> c.getClaimant().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new ConflictException("Apenas solicitações em análise podem ser canceladas");
        }

        claim.setStatus(ClaimStatus.CANCELLED);
        claimRepository.save(claim);
    }

    @Transactional(readOnly = true)
    public Page<EstablishmentClaimAdminResponseDTO> listAll(ClaimStatus status, Pageable pageable) {
        Page<EstablishmentClaim> page = status == null
                ? claimRepository.findAll(pageable)
                : claimRepository.findByStatus(status, pageable);
        return page.map(this::toAdminResponseDTO);
    }

    @Transactional(readOnly = true)
    public EstablishmentClaimAdminResponseDTO getById(UUID id) {
        return claimRepository.findById(id)
                .map(this::toAdminResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
    }

    @Transactional
    public EstablishmentClaimAdminResponseDTO approve(UUID claimId, SystemUser admin) {
        // O lock do spot vem antes do lock da claim: a aprovação altera as outras claims do mesmo spot,
        // então travar na ordem inversa causaria deadlock entre duas aprovações concorrentes.
        UUID spotId = claimRepository.findSpotIdById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
        touristSpotRepository.lockById(spotId)
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado"));

        EstablishmentClaim claim = claimRepository.findByIdForUpdate(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new ConflictException("Esta solicitação já foi analisada");
        }

        SystemUser claimant = userRepository.findByIdForUpdate(claim.getClaimant().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        TouristSpot spot = touristSpotRepository.findById(spotId)
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado"));

        if (spot.getOwner() != null) {
            throw new ConflictException("Este estabelecimento já possui responsável");
        }
        if (claimant.getUserRole() != Role.EMPRESA) {
            throw new ConflictException("O solicitante não possui mais uma conta empresarial");
        }
        if (hasEstablishment(claimant.getId())) {
            throw new ConflictException("O solicitante já possui um estabelecimento vinculado");
        }

        LocalDateTime now = LocalDateTime.now();

        spot.setOwner(claimant);
        touristSpotRepository.save(spot);

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setReviewedAt(now);
        claim.setReviewedBy(admin);

        for (EstablishmentClaim other : claimRepository.findBySpotIdAndStatus(spotId, ClaimStatus.PENDING)) {
            if (other.getId().equals(claim.getId())) {
                continue;
            }
            other.setStatus(ClaimStatus.REJECTED);
            other.setRejectionReason(AUTO_REJECTION_REASON);
            other.setReviewedAt(now);
            other.setReviewedBy(admin);
        }

        claimRepository.save(claim);
        return toAdminResponseDTO(claim);
    }

    @Transactional
    public EstablishmentClaimAdminResponseDTO reject(UUID claimId, String reason, SystemUser admin) {
        EstablishmentClaim claim = claimRepository.findByIdForUpdate(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new ConflictException("Esta solicitação já foi analisada");
        }

        String trimmed = reason == null ? null : reason.trim();
        claim.setStatus(ClaimStatus.REJECTED);
        claim.setRejectionReason(trimmed == null || trimmed.isEmpty() ? null : trimmed);
        claim.setReviewedAt(LocalDateTime.now());
        claim.setReviewedBy(admin);
        claimRepository.save(claim);
        return toAdminResponseDTO(claim);
    }

    private boolean hasEstablishment(UUID userId) {
        return restaurantRepository.findByOwnerId(userId).isPresent()
                || hostPointRepository.findByOwnerId(userId).isPresent();
    }

    private boolean isClaimableType(TouristSpot spot) {
        Object real = Hibernate.unproxy(spot);
        return real instanceof Restaurant || real instanceof HostPoint;
    }

    private EstablishmentType typeOf(TouristSpot spot) {
        return Hibernate.unproxy(spot) instanceof Restaurant ? EstablishmentType.RESTAURANT : EstablishmentType.HOST_POINT;
    }

    private EstablishmentClaimResponseDTO toResponseDTO(EstablishmentClaim claim) {
        TouristSpot spot = claim.getSpot();
        return new EstablishmentClaimResponseDTO(
                claim.getId(), spot.getId(), spot.getName(), typeOf(spot), claim.getStatus(),
                claim.getMessage(), claim.getContactPhone(), claim.getCreatedDate(),
                claim.getReviewedAt(), claim.getRejectionReason()
        );
    }

    private EstablishmentClaimAdminResponseDTO toAdminResponseDTO(EstablishmentClaim claim) {
        TouristSpot spot = claim.getSpot();
        SystemUser claimant = claim.getClaimant();
        EstablishmentType type = typeOf(spot);
        return new EstablishmentClaimAdminResponseDTO(
                claim.getId(), spot.getId(), spot.getName(), type, claim.getStatus(),
                claim.getMessage(), claim.getContactPhone(), claim.getCreatedDate(),
                claim.getReviewedAt(), claim.getRejectionReason(),
                new EstablishmentClaimAdminResponseDTO.Claimant(
                        claimant.getId(), fullName(claimant), claimant.getEmail(),
                        claimant.getCompanyName(), claimant.getCnpj(), claimant.getUserRole()),
                new EstablishmentClaimAdminResponseDTO.Spot(
                        spot.getId(), spot.getName(), type, spot.getAddress(), spot.getActive(),
                        spot.getApprovalStatus(), spot.getOwner() != null),
                claim.getReviewedBy() == null ? null : fullName(claim.getReviewedBy())
        );
    }

    private String fullName(SystemUser user) {
        return user.getLastName() == null || user.getLastName().isBlank()
                ? user.getFirstName()
                : user.getFirstName() + " " + user.getLastName();
    }
}
