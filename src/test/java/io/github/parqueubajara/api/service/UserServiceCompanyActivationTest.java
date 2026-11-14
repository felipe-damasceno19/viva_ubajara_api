package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.exception.DuplicateRegistry;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.enums.Role;
import io.github.parqueubajara.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceCompanyActivationTest {

    private static final String EMAIL = "user@example.com";
    private static final String CNPJ = "11222333000181";

    @Mock private UserRepository repository;
    @Mock private io.github.parqueubajara.api.mapper.UserMapper mapper;
    @Mock private org.springframework.security.crypto.password.PasswordEncoder encoder;
    @Mock private io.github.parqueubajara.api.service.infra.S3StorageService storageService;
    @Mock private io.github.parqueubajara.api.service.infra.FileValidationService validationService;
    @InjectMocks private UserService service;

    @Test
    void activatesExistingUserWithoutReplacingIdentityOrPersonalData() {
        SystemUser user = user(Role.USER);
        when(repository.findByEmailForUpdate(EMAIL)).thenReturn(Optional.of(user));
        when(repository.existsByCnpjInAndIdNot(anyCollection(), eq(user.getId()))).thenReturn(false);
        when(repository.save(user)).thenReturn(user);

        SystemUser result = service.activateCompany(EMAIL, "Empresa Exemplo", CNPJ);

        assertSame(user, result);
        assertEquals(Role.EMPRESA, user.getUserRole());
        assertEquals("Empresa Exemplo", user.getCompanyName());
        assertEquals(CNPJ, user.getCnpj());
        assertEquals(UUID.fromString("d4c40370-2701-4b20-ae49-4ae4f8be2bdd"), user.getId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals("existing-user", user.getUsername());
        assertEquals("password-hash", user.getPassword());
        assertEquals("Pessoa", user.getFirstName());
        assertEquals("https://example.com/photo.jpg", user.getPhotoUrl());
        verify(repository).save(user);
    }

    @Test
    void repeatedActivationWithSameDataDoesNotSaveOrCreateAnotherUser() {
        SystemUser user = user(Role.EMPRESA);
        user.setCompanyName("Empresa Exemplo");
        user.setCnpj(CNPJ);
        when(repository.findByEmailForUpdate(EMAIL)).thenReturn(Optional.of(user));

        assertSame(user, service.activateCompany(EMAIL, "Empresa Exemplo", CNPJ));

        verify(repository, never()).save(any(SystemUser.class));
        verify(repository, never()).existsByCnpjInAndIdNot(anyCollection(), any());
    }

    @Test
    void repeatedActivationCannotChangeCompanyData() {
        SystemUser user = user(Role.EMPRESA);
        user.setCompanyName("Empresa Original");
        user.setCnpj(CNPJ);
        when(repository.findByEmailForUpdate(EMAIL)).thenReturn(Optional.of(user));

        assertThrows(DuplicateRegistry.class,
                () -> service.activateCompany(EMAIL, "Outra Empresa", CNPJ));

        assertEquals("Empresa Original", user.getCompanyName());
        verify(repository, never()).save(any(SystemUser.class));
    }

    @Test
    void rejectsCnpjOwnedByAnotherUserWithoutChangingCurrentUser() {
        SystemUser user = user(Role.USER);
        when(repository.findByEmailForUpdate(EMAIL)).thenReturn(Optional.of(user));
        when(repository.existsByCnpjInAndIdNot(anyCollection(), eq(user.getId()))).thenReturn(true);

        assertThrows(DuplicateRegistry.class,
                () -> service.activateCompany(EMAIL, "Empresa Exemplo", CNPJ));

        assertEquals(Role.USER, user.getUserRole());
        assertNull(user.getCompanyName());
        assertNull(user.getCnpj());
        verify(repository, never()).save(any(SystemUser.class));
    }

    @Test
    void rejectsNonUserRoleForFirstActivation() {
        SystemUser user = user(Role.ADMIN);
        when(repository.findByEmailForUpdate(EMAIL)).thenReturn(Optional.of(user));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.activateCompany(EMAIL, "Empresa Exemplo", CNPJ));

        verify(repository, never()).save(any(SystemUser.class));
    }

    private SystemUser user(Role role) {
        SystemUser user = new SystemUser();
        user.setId(UUID.fromString("d4c40370-2701-4b20-ae49-4ae4f8be2bdd"));
        user.setFirstName("Pessoa");
        user.setLastName("Exemplo");
        user.setUsername("existing-user");
        user.setEmail(EMAIL);
        user.setPassword("password-hash");
        user.setUserRole(role);
        user.setPhotoUrl("https://example.com/photo.jpg");
        return user;
    }
}
