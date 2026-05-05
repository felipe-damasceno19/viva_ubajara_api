package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    @EntityGraph(attributePaths = {"photos"})
    Page<Event> findByStartDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Page<Event> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Optional<Event> findById(UUID id);
}
