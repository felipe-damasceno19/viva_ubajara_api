package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.request.ReviewRequestDTO;
import io.github.parqueubajara.api.dto.response.ReviewAdminResponseDTO;
import io.github.parqueubajara.api.dto.response.ReviewResponseDTO;
import io.github.parqueubajara.api.dto.response.ReviewSummaryDTO;
import io.github.parqueubajara.api.exception.ConflictException;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.model.Attraction;
import io.github.parqueubajara.api.model.Restaurant;
import io.github.parqueubajara.api.model.Review;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.TouristSpot;
import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import io.github.parqueubajara.api.model.enums.Role;
import io.github.parqueubajara.api.repository.ReviewRepository;
import io.github.parqueubajara.api.repository.TouristSpotRepository;
import io.github.parqueubajara.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private TouristSpotRepository touristSpotRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ReviewService service;

    @Test
    void upsertCreatesPendingReviewWithTrimmedComment() {
        SystemUser user = user();
        Restaurant spot = spot(null, true, ApprovalStatus.APPROVED);
        stubPreconditions(user, spot);
        when(reviewRepository.findBySpotIdAndAuthorId(spot.getId(), user.getId())).thenReturn(Optional.empty());
        when(reviewRepository.saveAndFlush(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewResponseDTO response = service.upsert(user, spot.getId(), new ReviewRequestDTO(5, "  Comida excelente  "));

        assertEquals(5, response.rating());
        assertEquals("Comida excelente", response.comment());
        assertEquals(ApprovalStatus.PENDING, response.status());
        assertEquals("Pessoa T.", response.authorName());
    }

    @Test
    void upsertStoresBlankCommentAsNull() {
        SystemUser user = user();
        Restaurant spot = spot(null, true, ApprovalStatus.APPROVED);
        stubPreconditions(user, spot);
        when(reviewRepository.findBySpotIdAndAuthorId(spot.getId(), user.getId())).thenReturn(Optional.empty());
        when(reviewRepository.saveAndFlush(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewResponseDTO response = service.upsert(user, spot.getId(), new ReviewRequestDTO(4, "   "));

        assertNull(response.comment());
    }

    @Test
    void upsertRefusesShortComment() {
        SystemUser user = user();
        Restaurant spot = spot(null, true, ApprovalStatus.APPROVED);
        stubPreconditions(user, spot);

        assertThrows(IllegalArgumentException.class,
                () -> service.upsert(user, spot.getId(), new ReviewRequestDTO(4, "curto")));
        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void upsertUpdatesExistingRowInsteadOfDuplicatingAndResetsToPending() {
        SystemUser user = user();
        Restaurant spot = spot(null, true, ApprovalStatus.APPROVED);
        Review existing = review(spot, user, 2, ApprovalStatus.APPROVED);
        stubPreconditions(user, spot);
        when(reviewRepository.findBySpotIdAndAuthorId(spot.getId(), user.getId())).thenReturn(Optional.of(existing));
        when(reviewRepository.saveAndFlush(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        service.upsert(user, spot.getId(), new ReviewRequestDTO(5, null));

        ArgumentCaptor<Review> saved = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository, times(1)).saveAndFlush(saved.capture());
        assertSame(existing, saved.getValue());
        assertEquals(5, existing.getRating());
        assertEquals(ApprovalStatus.PENDING, existing.getStatus());
    }

    @Test
    void upsertRefusesOwnerOfTheSpot() {
        SystemUser user = user();
        Restaurant spot = spot(user, true, ApprovalStatus.APPROVED);
        stubPreconditions(user, spot);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> service.upsert(user, spot.getId(), new ReviewRequestDTO(5, null)));

        assertEquals("Você não pode avaliar o seu próprio estabelecimento", ex.getMessage());
        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void upsertRefusesInactiveSpot() {
        SystemUser user = user();
        Restaurant spot = spot(null, false, ApprovalStatus.APPROVED);
        stubPreconditions(user, spot);

        assertThrows(ResourceNotFoundException.class,
                () -> service.upsert(user, spot.getId(), new ReviewRequestDTO(5, null)));
        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void upsertRefusesSpotNotApproved() {
        SystemUser user = user();
        Restaurant spot = spot(null, true, ApprovalStatus.PENDING);
        stubPreconditions(user, spot);

        assertThrows(ResourceNotFoundException.class,
                () -> service.upsert(user, spot.getId(), new ReviewRequestDTO(5, null)));
    }

    @Test
    void upsertTurnsUniqueIndexViolationIntoConflict() {
        SystemUser user = user();
        Restaurant spot = spot(null, true, ApprovalStatus.APPROVED);
        stubPreconditions(user, spot);
        when(reviewRepository.findBySpotIdAndAuthorId(spot.getId(), user.getId())).thenReturn(Optional.empty());
        when(reviewRepository.saveAndFlush(any(Review.class)))
                .thenThrow(new DataIntegrityViolationException("ux_review_spot_author"));

        assertThrows(ConflictException.class,
                () -> service.upsert(user, spot.getId(), new ReviewRequestDTO(5, null)));
    }

    @Test
    void summaryComputesAverageAndFillsMissingStarsWithZero() {
        UUID spotId = UUID.randomUUID();
        when(reviewRepository.distributionBySpotId(spotId))
                .thenReturn(List.of(new Object[]{5, 2L}, new Object[]{4, 1L}));

        ReviewSummaryDTO summary = service.summary(spotId);

        assertEquals(3, summary.count());
        assertEquals(4.7, summary.average());
        assertEquals(Map.of(1, 0L, 2, 0L, 3, 0L, 4, 1L, 5, 2L), summary.distribution());
        assertEquals(List.of(1, 2, 3, 4, 5), List.copyOf(summary.distribution().keySet()));
    }

    @Test
    void summaryWithoutApprovedReviewsHasNullAverage() {
        UUID spotId = UUID.randomUUID();
        when(reviewRepository.distributionBySpotId(spotId)).thenReturn(List.of());

        ReviewSummaryDTO summary = service.summary(spotId);

        assertNull(summary.average());
        assertEquals(0, summary.count());
        assertEquals(5, summary.distribution().size());
    }

    @Test
    void summariesForUsesASingleBatchQuery() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        when(reviewRepository.summarizeBySpotIds(List.of(a, b, c)))
                .thenReturn(List.<Object[]>of(new Object[]{a, 4.25, 4L}, new Object[]{b, 5.0, 1L}));

        Map<UUID, ReviewService.RatingSummary> result = service.summariesFor(List.of(a, b, c));

        verify(reviewRepository, times(1)).summarizeBySpotIds(any());
        assertEquals(new ReviewService.RatingSummary(4.3, 4), result.get(a));
        assertEquals(new ReviewService.RatingSummary(5.0, 1), result.get(b));
        assertFalse(result.containsKey(c));
    }

    @Test
    void summariesForEmptyCollectionSkipsTheQuery() {
        assertTrue(service.summariesFor(List.of()).isEmpty());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void listApprovedQueriesApprovedAndDoesNotExposeEmail() {
        SystemUser author = user();
        Restaurant spot = spot(null, true, ApprovalStatus.APPROVED);
        Review review = review(spot, author, 4, ApprovalStatus.APPROVED);
        PageRequest pageable = PageRequest.of(0, 5);
        when(reviewRepository.findBySpotIdAndStatus(spot.getId(), ApprovalStatus.APPROVED, pageable))
                .thenReturn(new PageImpl<>(List.of(review)));

        ReviewResponseDTO dto = service.listApproved(spot.getId(), pageable).getContent().get(0);

        assertEquals("Pessoa T.", dto.authorName());
        assertFalse(dto.toString().contains(author.getEmail()));
    }

    @Test
    void listForModerationReportsSpotTypeAndAuthorEmail() {
        SystemUser author = user();
        Attraction attraction = new Attraction();
        attraction.setId(UUID.randomUUID());
        attraction.setName("Cachoeira");
        Review review = review(attraction, author, 3, ApprovalStatus.PENDING);
        PageRequest pageable = PageRequest.of(0, 10);
        when(reviewRepository.findByStatus(ApprovalStatus.PENDING, pageable))
                .thenReturn(new PageImpl<>(List.of(review)));

        ReviewAdminResponseDTO dto = service.listForModeration(ApprovalStatus.PENDING, pageable).getContent().get(0);

        assertEquals("ATTRACTION", dto.spot().type());
        assertEquals(author.getEmail(), dto.author().email());
    }

    @Test
    void updateStatusChangesTheReview() {
        Review review = review(spot(null, true, ApprovalStatus.APPROVED), user(), 5, ApprovalStatus.PENDING);
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        service.updateStatus(review.getId(), ApprovalStatus.APPROVED);

        assertEquals(ApprovalStatus.APPROVED, review.getStatus());
        verify(reviewRepository).save(review);
    }

    @Test
    void deleteMissingReviewIs404() {
        UUID id = UUID.randomUUID();
        when(reviewRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
        verify(reviewRepository, never()).deleteById(any());
    }

    private void stubPreconditions(SystemUser user, TouristSpot spot) {
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(touristSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));
    }

    private SystemUser user() {
        SystemUser user = new SystemUser();
        user.setId(UUID.randomUUID());
        user.setFirstName("Pessoa");
        user.setLastName("Teste");
        user.setEmail(user.getId() + "@example.com");
        user.setUserRole(Role.USER);
        return user;
    }

    private Restaurant spot(SystemUser owner, boolean active, ApprovalStatus status) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.randomUUID());
        restaurant.setName("Restaurante");
        restaurant.setOwner(owner);
        restaurant.setActive(active);
        restaurant.setApprovalStatus(status);
        return restaurant;
    }

    private Review review(TouristSpot spot, SystemUser author, int rating, ApprovalStatus status) {
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setSpot(spot);
        review.setAuthor(author);
        review.setRating(rating);
        review.setStatus(status);
        return review;
    }
}
