package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID> {

    Page<ContactMessage> findByRead(Boolean read, Pageable pageable);

    long countByRead(Boolean read);
}
