package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.Review;
import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @EntityGraph(attributePaths = {"author"})
    Page<Review> findBySpotIdAndStatus(UUID spotId, ApprovalStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    Optional<Review> findBySpotIdAndAuthorId(UUID spotId, UUID authorId);

    @Override
    @EntityGraph(attributePaths = {"spot", "author"})
    Page<Review> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"spot", "author"})
    Page<Review> findByStatus(ApprovalStatus status, Pageable pageable);

    @Query("select r.spot.id, avg(r.rating), count(r) from Review r "
            + "where r.status = io.github.parqueubajara.api.model.enums.ApprovalStatus.APPROVED "
            + "and r.spot.id in :ids group by r.spot.id")
    List<Object[]> summarizeBySpotIds(@Param("ids") Collection<UUID> ids);

    @Query("select r.rating, count(r) from Review r "
            + "where r.status = io.github.parqueubajara.api.model.enums.ApprovalStatus.APPROVED "
            + "and r.spot.id = :spotId group by r.rating")
    List<Object[]> distributionBySpotId(@Param("spotId") UUID spotId);
}
