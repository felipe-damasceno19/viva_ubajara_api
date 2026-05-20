package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<SystemUser, UUID> {

    Page<SystemUser> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    Page<SystemUser> findByUserRole(Role role, Pageable pageable);

    Page<SystemUser> findByUsernameContainingIgnoreCaseAndUserRole(String username, Role role, Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsById(UUID id);

    boolean existsByUsername(String username);

    Optional<SystemUser> findById(UUID id);

    Optional<SystemUser> findByEmail(String email);
}
