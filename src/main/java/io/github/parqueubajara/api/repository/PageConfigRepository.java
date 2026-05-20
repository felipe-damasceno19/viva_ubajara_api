package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.PageConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageConfigRepository extends JpaRepository<PageConfig, String> {
}
