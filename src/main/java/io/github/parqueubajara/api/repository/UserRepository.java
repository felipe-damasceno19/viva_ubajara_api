package io.github.parqueubajara.api.repository;

import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.enums.Role;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjIn(Collection<String> cnpjs);

    boolean existsByCnpjInAndIdNot(Collection<String> cnpjs, UUID id);

    Optional<SystemUser> findById(UUID id);

    Optional<SystemUser> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from SystemUser u where u.email = :email")
    Optional<SystemUser> findByEmailForUpdate(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from SystemUser u where u.id = :id")
    Optional<SystemUser> findByIdForUpdate(@Param("id") UUID id);
}
