package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.TourGuide;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourGuideRepository extends JpaRepository<TourGuide, UUID> {

    boolean existsByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Page<TourGuide> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Optional<TourGuide> findById(UUID id);
}
