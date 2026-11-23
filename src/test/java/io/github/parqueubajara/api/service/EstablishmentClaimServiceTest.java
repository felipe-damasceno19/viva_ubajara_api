package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.request.EstablishmentClaimRequestDTO;
import io.github.parqueubajara.api.dto.response.EstablishmentClaimAdminResponseDTO;
import io.github.parqueubajara.api.dto.response.EstablishmentClaimResponseDTO;
import io.github.parqueubajara.api.exception.ConflictException;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import io.github.parqueubajara.api.exception.TooManyRequestsException;
import io.github.parqueubajara.api.model.Attraction;
import io.github.parqueubajara.api.model.EstablishmentClaim;
import io.github.parqueubajara.api.model.HostPoint;
import io.github.parqueubajara.api.model.Restaurant;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.TouristSpot;
import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import io.github.parqueubajara.api.model.enums.ClaimStatus;
import io.github.parqueubajara.api.model.enums.EstablishmentType;
import io.github.parqueubajara.api.model.enums.Role;
import io.github.parqueubajara.api.repository.EstablishmentClaimRepository;
import io.github.parqueubajara.api.repository.HostPointRepository;
import io.github.parqueubajara.api.repository.RestaurantRepository;
import io.github.parqueubajara.api.repository.TouristSpotRepository;
import io.github.parqueubajara.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstablishmentClaimServiceTest {

    private static final String AUTO_REASON = "Outra solicitação foi aprovada para este estabelecimento.";

    @Mock private EstablishmentClaimRepository claimRepository;
    @Mock private TouristSpotRepository touristSpotRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private HostPointRepository hostPointRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private EstablishmentClaimService service;

    @Test
    void createStoresPendingClaimForClaimableRestaurant() {
        SystemUser user = user(Role.EMPRESA);
        Restaurant spot = restaurant(null, ApprovalStatus.APPROVED, true);
        stubCreatePreconditions(user, spot);
        when(claimRepository.save(any(EstablishmentClaim.class))).thenAnswer(inv -> inv.getArgument(0));

        EstablishmentClaimResponseDTO response = service.create(user,
                new EstablishmentClaimRequestDTO(spot.getId(), "  Sou o dono do local  ", " (88) 99999-0000 "));

        assertEquals(ClaimStatus.PENDING, response.status());
        assertEquals(EstablishmentType.RESTAURANT, response.spotType());
        assertEquals(spot.getId(), response.spotId());
        assertEquals("Sou o dono do local", response.message());
        assertEquals("(88) 99999-0000", response.contactPhone());
    }

    @Test
    void createAcceptsHostPoint() {
        SystemUser user = user(Role.EMPRESA);
        HostPoint spot = hostPoint(null, ApprovalStatus.APPROVED, true);
        stubCreatePreconditions(user, spot);
        when(claimRepository.save(any(EstablishmentClaim.class))).thenAnswer(inv -> inv.getArgument(0));

        EstablishmentClaimResponseDTO response = service.create(user, request(spot.getId()));

        assertEquals(EstablishmentType.HOST_POINT, response.spotType());
    }

    @Test
    void createRefusesNonCompanyUser() {
        SystemUser user = user(Role.USER);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> service.create(user, request(UUID.randomUUID())));
        verify(claimRepository, never()).save(any());
    }

    @Test
    void createRefusesAdmin() {
        SystemUser user = user(Role.ADMIN);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> service.create(user, request(UUID.randomUUID())));
    }

    @Test
    void createRefusesUnknownSpot() {
        SystemUser user = user(Role.EMPRESA);
        UUID spotId = UUID.randomUUID();
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(touristSpotRepository.findById(spotId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(user, request(spotId)));
    }

    @Test
    void createRefusesAttraction() {
        SystemUser user = user(Role.EMPRESA);
        Attraction attraction = new Attraction();
        attraction.setId(UUID.randomUUID());
        attraction.setApprovalStatus(ApprovalStatus.APPROVED);
        attraction.setActive(true);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(touristSpotRepository.findById(attraction.getId())).thenReturn(Optional.of(attraction));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(user, request(attraction.getId())));
        assertEquals("Este local não pode ser reivindicado", ex.getMessage());
    }

    @Test
    void createRefusesSpotWithOwner() {
        SystemUser user = user(Role.EMPRESA);
        Restaurant spot = restaurant(user(Role.EMPRESA), ApprovalStatus.APPROVED, true);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(touristSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));

        assertThrows(ConflictException.class, () -> service.create(user, request(spot.getId())));
        verify(claimRepository, never()).save(any());
    }

    @Test
    void createRefusesPendingSpot() {
        SystemUser user = user(Role.EMPRESA);
        Restaurant spot = restaurant(null, ApprovalStatus.PENDING, true);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(touristSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));

        assertThrows(ConflictException.class, () -> service.create(user, request(spot.getId())));
    }

    @Test
    void createRefusesInactiveSpot() {
        SystemUser user = user(Role.EMPRESA);
        Restaurant spot = restaurant(null, ApprovalStatus.APPROVED, false);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(touristSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));

        assertThrows(ConflictException.class, () -> service.create(user, request(spot.getId())));
    }

    @Test
    void createRefusesUserWithRestaurant() {
        SystemUser user = user(Role.EMPRESA);
        Restaurant spot = restaurant(null, ApprovalStatus.APPROVED, true);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(touristSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));
        when(restaurantRepository.findByOwnerId(user.getId())).thenReturn(Optional.of(new Restaurant()));

        assertThrows(ConflictException.class, () -> service.create(user, request(spot.getId())));
        verify(claimRepository, never()).save(any());
    }

    @Test
    void createRefusesUserWithHostPoint() {
        SystemUser user = user(Role.EMPRESA);
        Restaurant spot = restaurant(null, ApprovalStatus.APPROVED, true);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(touristSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));
        when(restaurantRepository.findByOwnerId(user.getId())).thenReturn(Optional.empty());
        when(hostPointRepository.findByOwnerId(user.getId())).thenReturn(Optional.of(new HostPoint()));

        assertThrows(ConflictException.class, () -> service.create(user, request(spot.getId())));
    }

    @Test
    void createRefusesWhenUserAlreadyHasPendingClaim() {
        SystemUser user = user(Role.EMPRESA);
        Restaurant spot = restaurant(null, ApprovalStatus.APPROVED, true);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(touristSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));
        when(claimRepository.existsByClaimantIdAndStatus(user.getId(), ClaimStatus.PENDING)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(user, request(spot.getId())));
        verify(claimRepository, never()).save(any());
    }

    @Test
    void createRefusesAfterDailyLimit() {
        SystemUser user = user(Role.EMPRESA);
        Restaurant spot = restaurant(null, ApprovalStatus.APPROVED, true);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(touristSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));
        when(claimRepository.countByClaimantIdAndCreatedDateAfter(eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(5L);

        assertThrows(TooManyRequestsException.class, () -> service.create(user, request(spot.getId())));
        verify(claimRepository, never()).save(any());
    }

    @Test
    void cancelMovesOwnPendingClaimToCancelled() {
        SystemUser user = user(Role.EMPRESA);
        EstablishmentClaim claim = claim(user, restaurant(null, ApprovalStatus.APPROVED, true), ClaimStatus.PENDING);
        when(claimRepository.findByIdForUpdate(claim.getId())).thenReturn(Optional.of(claim));

        service.cancel(user, claim.getId());

        assertEquals(ClaimStatus.CANCELLED, claim.getStatus());
        verify(claimRepository).save(claim);
    }

    @Test
    void cancelHidesClaimFromOtherUsers() {
        SystemUser owner = user(Role.EMPRESA);
        SystemUser other = user(Role.EMPRESA);
        EstablishmentClaim claim = claim(owner, restaurant(null, ApprovalStatus.APPROVED, true), ClaimStatus.PENDING);
        when(claimRepository.findByIdForUpdate(claim.getId())).thenReturn(Optional.of(claim));

        assertThrows(ResourceNotFoundException.class, () -> service.cancel(other, claim.getId()));
        assertEquals(ClaimStatus.PENDING, claim.getStatus());
        verify(claimRepository, never()).save(any());
    }

    @Test
    void cancelRefusesClaimAlreadyReviewed() {
        SystemUser user = user(Role.EMPRESA);
        EstablishmentClaim claim = claim(user, restaurant(null, ApprovalStatus.APPROVED, true), ClaimStatus.APPROVED);
        when(claimRepository.findByIdForUpdate(claim.getId())).thenReturn(Optional.of(claim));

        assertThrows(ConflictException.class, () -> service.cancel(user, claim.getId()));
        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
    }

    @Test
    void approveSetsOwnerAndRejectsOtherPendingClaimsOfSameSpot() {
        SystemUser admin = user(Role.ADMIN);
        SystemUser claimant = user(Role.EMPRESA);
        SystemUser rival = user(Role.EMPRESA);
        Restaurant spot = restaurant(null, ApprovalStatus.APPROVED, true);
        EstablishmentClaim claim = claim(claimant, spot, ClaimStatus.PENDING);
        EstablishmentClaim rivalClaim = claim(rival, spot, ClaimStatus.PENDING);
        stubApprovePreconditions(claim, claimant, spot);
        when(claimRepository.findBySpotIdAndStatus(spot.getId(), ClaimStatus.PENDING))
                .thenReturn(List.of(claim, rivalClaim));

        EstablishmentClaimAdminResponseDTO response = service.approve(claim.getId(), admin);

        assertSame(claimant, spot.getOwner());
        assertEquals(ApprovalStatus.APPROVED, spot.getApprovalStatus());
        assertTrue(spot.getActive());
        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
        assertSame(admin, claim.getReviewedBy());
        assertNotNull(claim.getReviewedAt());
        assertEquals(ClaimStatus.REJECTED, rivalClaim.getStatus());
        assertEquals(AUTO_REASON, rivalClaim.getRejectionReason());
        assertSame(admin, rivalClaim.getReviewedBy());
        assertNotNull(rivalClaim.getReviewedAt());
        assertEquals(ClaimStatus.APPROVED, response.status());
        assertTrue(response.spot().hasOwner());
        verify(touristSpotRepository).save(spot);
        var order = inOrder(touristSpotRepository, claimRepository);
        order.verify(touristSpotRepository).lockById(spot.getId());
        order.verify(claimRepository).findByIdForUpdate(claim.getId());
    }

    @Test
    void approveRefusesClaimAlreadyReviewed() {
        Restaurant spot = restaurant(null, ApprovalStatus.APPROVED, true);
        EstablishmentClaim claim = claim(user(Role.EMPRESA), spot, ClaimStatus.REJECTED);
        stubSpotLock(claim, spot);
        when(claimRepository.findByIdForUpdate(claim.getId())).thenReturn(Optional.of(claim));

        assertThrows(ConflictException.class, () -> service.approve(claim.getId(), user(Role.ADMIN)));
        verify(touristSpotRepository, never()).save(any());
    }

    @Test
    void approveRefusesSpotThatAlreadyHasOwner() {
        SystemUser claimant = user(Role.EMPRESA);
        Restaurant spot = restaurant(user(Role.EMPRESA), ApprovalStatus.APPROVED, true);
        EstablishmentClaim claim = claim(claimant, spot, ClaimStatus.PENDING);
        stubApprovePreconditions(claim, claimant, spot);

        assertThrows(ConflictException.class, () -> service.approve(claim.getId(), user(Role.ADMIN)));
        assertEquals(ClaimStatus.PENDING, claim.getStatus());
        verify(touristSpotRepository, never()).save(any());
    }

    @Test
    void approveRefusesClaimantThatIsNoLongerCompany() {
        SystemUser claimant = user(Role.USER);
        Restaurant spot = restaurant(null, ApprovalStatus.APPROVED, true);
        EstablishmentClaim claim = claim(claimant, spot, ClaimStatus.PENDING);
        stubApprovePreconditions(claim, claimant, spot);

        assertThrows(ConflictException.class, () -> service.approve(claim.getId(), user(Role.ADMIN)));
        assertNull(spot.getOwner());
        verify(touristSpotRepository, never()).save(any());
    }

    @Test
    void approveRefusesClaimantThatAlreadyHasEstablishment() {
        SystemUser claimant = user(Role.EMPRESA);
        Restaurant spot = restaurant(null, ApprovalStatus.APPROVED, true);
        EstablishmentClaim claim = claim(claimant, spot, ClaimStatus.PENDING);
        stubApprovePreconditions(claim, claimant, spot);
        when(restaurantRepository.findByOwnerId(claimant.getId())).thenReturn(Optional.of(new Restaurant()));

        assertThrows(ConflictException.class, () -> service.approve(claim.getId(), user(Role.ADMIN)));
        assertNull(spot.getOwner());
        assertEquals(ClaimStatus.PENDING, claim.getStatus());
        verify(touristSpotRepository, never()).save(any());
    }

    @Test
    void rejectStoresTrimmedReason() {
        SystemUser admin = user(Role.ADMIN);
        EstablishmentClaim claim = claim(user(Role.EMPRESA), restaurant(null, ApprovalStatus.APPROVED, true), ClaimStatus.PENDING);
        when(claimRepository.findByIdForUpdate(claim.getId())).thenReturn(Optional.of(claim));

        service.reject(claim.getId(), "  Documentação insuficiente  ", admin);

        assertEquals(ClaimStatus.REJECTED, claim.getStatus());
        assertEquals("Documentação insuficiente", claim.getRejectionReason());
        assertSame(admin, claim.getReviewedBy());
        assertNotNull(claim.getReviewedAt());
    }

    @Test
    void rejectWithoutReasonLeavesItEmpty() {
        EstablishmentClaim claim = claim(user(Role.EMPRESA), restaurant(null, ApprovalStatus.APPROVED, true), ClaimStatus.PENDING);
        when(claimRepository.findByIdForUpdate(claim.getId())).thenReturn(Optional.of(claim));

        service.reject(claim.getId(), "   ", user(Role.ADMIN));

        assertEquals(ClaimStatus.REJECTED, claim.getStatus());
        assertNull(claim.getRejectionReason());
    }

    @Test
    void rejectRefusesClaimAlreadyReviewed() {
        EstablishmentClaim claim = claim(user(Role.EMPRESA), restaurant(null, ApprovalStatus.APPROVED, true), ClaimStatus.APPROVED);
        when(claimRepository.findByIdForUpdate(claim.getId())).thenReturn(Optional.of(claim));

        assertThrows(ConflictException.class, () -> service.reject(claim.getId(), null, user(Role.ADMIN)));
        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
    }

    private void stubCreatePreconditions(SystemUser user, TouristSpot spot) {
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(touristSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));
    }

    private void stubApprovePreconditions(EstablishmentClaim claim, SystemUser claimant, TouristSpot spot) {
        stubSpotLock(claim, spot);
        when(claimRepository.findByIdForUpdate(claim.getId())).thenReturn(Optional.of(claim));
        when(userRepository.findByIdForUpdate(claimant.getId())).thenReturn(Optional.of(claimant));
        when(touristSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));
    }

    private void stubSpotLock(EstablishmentClaim claim, TouristSpot spot) {
        when(claimRepository.findSpotIdById(claim.getId())).thenReturn(Optional.of(spot.getId()));
        when(touristSpotRepository.lockById(spot.getId())).thenReturn(Optional.of(spot.getId()));
    }

    private EstablishmentClaimRequestDTO request(UUID spotId) {
        return new EstablishmentClaimRequestDTO(spotId, "Sou o proprietário deste local", "88999990000");
    }

    private SystemUser user(Role role) {
        SystemUser user = new SystemUser();
        user.setId(UUID.randomUUID());
        user.setFirstName("Pessoa");
        user.setLastName("Teste");
        user.setEmail(user.getId() + "@example.com");
        user.setUserRole(role);
        return user;
    }

    private Restaurant restaurant(SystemUser owner, ApprovalStatus status, boolean active) {
        Restaurant restaurant = new Restaurant();
        fill(restaurant, owner, status, active);
        return restaurant;
    }

    private HostPoint hostPoint(SystemUser owner, ApprovalStatus status, boolean active) {
        HostPoint hostPoint = new HostPoint();
        fill(hostPoint, owner, status, active);
        return hostPoint;
    }

    private void fill(TouristSpot spot, SystemUser owner, ApprovalStatus status, boolean active) {
        spot.setId(UUID.randomUUID());
        spot.setName("Local Exemplo");
        spot.setOwner(owner);
        spot.setApprovalStatus(status);
        spot.setActive(active);
    }

    private EstablishmentClaim claim(SystemUser claimant, TouristSpot spot, ClaimStatus status) {
        EstablishmentClaim claim = new EstablishmentClaim();
        claim.setId(UUID.randomUUID());
        claim.setClaimant(claimant);
        claim.setSpot(spot);
        claim.setStatus(status);
        claim.setMessage("Sou o proprietário deste local");
        claim.setContactPhone("88999990000");
        return claim;
    }
}
