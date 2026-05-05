package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    boolean existsByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Page<Restaurant> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"photos"})
    Optional<Restaurant> findById(UUID id);
}
