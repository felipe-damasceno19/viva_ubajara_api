package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.GastronomyItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GastronomyItemRepository extends JpaRepository<GastronomyItem, UUID> {

    List<GastronomyItem> findByActiveTrueOrderByDisplayOrderAscNameAsc();
}
