package io.github.parqueubajara.api.mapper;

import io.github.parqueubajara.api.model.HostPoint;
import io.github.parqueubajara.api.model.Restaurant;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.TouristSpot;
import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaimableMappingTest {

    private final RestaurantMapper restaurantMapper = new RestaurantMapperImpl();
    private final HostPointMapper hostPointMapper = new HostPointMapperImpl();

    @Test
    void restaurantIsClaimableOnlyWithoutOwnerApprovedAndActive() {
        assertTrue(restaurantMapper.toResponseDTO(restaurant(null, ApprovalStatus.APPROVED, true)).claimable());
        assertFalse(restaurantMapper.toResponseDTO(restaurant(new SystemUser(), ApprovalStatus.APPROVED, true)).claimable());
        assertFalse(restaurantMapper.toResponseDTO(restaurant(null, ApprovalStatus.PENDING, true)).claimable());
        assertFalse(restaurantMapper.toResponseDTO(restaurant(null, ApprovalStatus.REJECTED, true)).claimable());
        assertFalse(restaurantMapper.toResponseDTO(restaurant(null, ApprovalStatus.APPROVED, false)).claimable());
        assertFalse(restaurantMapper.toResponseDTO(restaurant(null, ApprovalStatus.APPROVED, null)).claimable());
    }

    @Test
    void hostPointIsClaimableOnlyWithoutOwnerApprovedAndActive() {
        assertTrue(hostPointMapper.toResponseDTO(hostPoint(null, ApprovalStatus.APPROVED, true)).claimable());
        assertFalse(hostPointMapper.toResponseDTO(hostPoint(new SystemUser(), ApprovalStatus.APPROVED, true)).claimable());
        assertFalse(hostPointMapper.toResponseDTO(hostPoint(null, ApprovalStatus.PENDING, true)).claimable());
        assertFalse(hostPointMapper.toResponseDTO(hostPoint(null, ApprovalStatus.REJECTED, true)).claimable());
        assertFalse(hostPointMapper.toResponseDTO(hostPoint(null, ApprovalStatus.APPROVED, false)).claimable());
    }

    private Restaurant restaurant(SystemUser owner, ApprovalStatus status, Boolean active) {
        Restaurant restaurant = new Restaurant();
        fill(restaurant, owner, status, active);
        return restaurant;
    }

    private HostPoint hostPoint(SystemUser owner, ApprovalStatus status, Boolean active) {
        HostPoint hostPoint = new HostPoint();
        fill(hostPoint, owner, status, active);
        return hostPoint;
    }

    private void fill(TouristSpot spot, SystemUser owner, ApprovalStatus status, Boolean active) {
        spot.setId(UUID.randomUUID());
        spot.setOwner(owner);
        spot.setApprovalStatus(status);
        spot.setActive(active);
    }
}
