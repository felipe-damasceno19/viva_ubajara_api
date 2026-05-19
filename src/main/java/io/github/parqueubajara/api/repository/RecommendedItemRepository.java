package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.RecommendedItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecommendedItemRepository extends JpaRepository<RecommendedItem, UUID> {

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Page<RecommendedItem> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Optional<RecommendedItem> findById(UUID id);

    @EntityGraph(attributePaths = {"photos"})
    Page<RecommendedItem> findByActive(Boolean active, Pageable pageable);

    @EntityGraph(attributePaths = {"photos"})
    Page<RecommendedItem> findByFeatured(Boolean featured, Pageable pageable);

    @EntityGraph(attributePaths = {"photos"})
    Page<RecommendedItem> findByCategory(String category, Pageable pageable);

    @EntityGraph(attributePaths = {"photos"})
    Page<RecommendedItem> findByActiveAndFeatured(Boolean active, Boolean featured, Pageable pageable);
}
