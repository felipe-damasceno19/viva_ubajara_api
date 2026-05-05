package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.Attraction;
import io.github.parqueubajara.api.model.enums.AttractionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, UUID> {

    @EntityGraph(attributePaths = {"photos"})
    Page<Attraction> findByCategory(AttractionType category, Pageable pageable);

    boolean existsByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Page<Attraction> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Optional<Attraction> findById(UUID id);
}
