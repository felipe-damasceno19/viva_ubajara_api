package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.Airport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AirportRepository extends JpaRepository<Airport, UUID> {

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Page<Airport> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Optional<Airport> findById(UUID id);

    boolean existsByIataCode(String iataCode);
}
