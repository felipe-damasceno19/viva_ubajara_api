package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.HostPoint;
import io.github.parqueubajara.api.model.enums.HostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HostPointRepository extends JpaRepository<HostPoint, UUID> {

    @EntityGraph(attributePaths = {"photos"})
    Page<HostPoint> findByHostType(HostType hostType, Pageable pageable);

    boolean existsByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Page<HostPoint> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Optional<HostPoint> findById(UUID id);


}
