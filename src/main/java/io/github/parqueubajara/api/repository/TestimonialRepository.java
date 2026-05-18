package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.Testimonial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, UUID> {

    Page<Testimonial> findByApproved(Boolean approved, Pageable pageable);
}
