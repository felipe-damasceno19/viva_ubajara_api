package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.EstablishmentClaim;
import io.github.parqueubajara.api.model.enums.ClaimStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstablishmentClaimRepository extends JpaRepository<EstablishmentClaim, UUID> {

    @Override
    @EntityGraph(attributePaths = {"spot", "claimant", "reviewedBy"})
    Page<EstablishmentClaim> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"spot", "claimant", "reviewedBy"})
    Optional<EstablishmentClaim> findById(UUID id);

    @EntityGraph(attributePaths = {"spot", "claimant", "reviewedBy"})
    Page<EstablishmentClaim> findByStatus(ClaimStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"spot", "claimant", "reviewedBy"})
    List<EstablishmentClaim> findByClaimantIdOrderByCreatedDateDesc(UUID claimantId);

    boolean existsByClaimantIdAndStatus(UUID claimantId, ClaimStatus status);

    long countByClaimantIdAndCreatedDateAfter(UUID claimantId, LocalDateTime after);

    List<EstablishmentClaim> findBySpotIdAndStatus(UUID spotId, ClaimStatus status);

    @Query("select c.spot.id from EstablishmentClaim c where c.id = :id")
    Optional<UUID> findSpotIdById(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from EstablishmentClaim c where c.id = :id")
    Optional<EstablishmentClaim> findByIdForUpdate(@Param("id") UUID id);
}
