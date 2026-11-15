package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.ClaimRejectionRequestDTO;
import io.github.parqueubajara.api.dto.request.EstablishmentClaimRequestDTO;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.enums.ClaimStatus;
import io.github.parqueubajara.api.service.CompanyService;
import io.github.parqueubajara.api.service.EstablishmentClaimService;
import io.github.parqueubajara.api.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EstablishmentClaimSecurityTest.TestConfiguration.class)
class EstablishmentClaimSecurityTest {

    @Autowired private EstablishmentClaimController adminController;
    @Autowired private CompanyController companyController;
    @Autowired private EstablishmentClaimService claimService;
    @Autowired private UserService userService;

    @BeforeEach
    void resetMocks() {
        reset(claimService, userService);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminRoutesRefuseCommonUser() {
        Authentication auth = login("user@example.com", "ROLE_USER");

        assertThrows(AccessDeniedException.class, () -> adminController.listAll(null, PageRequest.of(0, 10)));
        assertThrows(AccessDeniedException.class, () -> adminController.getById(UUID.randomUUID()));
        assertThrows(AccessDeniedException.class, () -> adminController.approve(UUID.randomUUID(), auth));
        assertThrows(AccessDeniedException.class, () -> adminController.reject(UUID.randomUUID(), null, auth));
        verifyNoInteractions(claimService);
    }

    @Test
    void adminRoutesRefuseCompanyUser() {
        Authentication auth = login("empresa@example.com", "ROLE_EMPRESA");

        assertThrows(AccessDeniedException.class, () -> adminController.approve(UUID.randomUUID(), auth));
        assertThrows(AccessDeniedException.class, () -> adminController.listAll(ClaimStatus.PENDING, PageRequest.of(0, 10)));
        verifyNoInteractions(claimService);
    }

    @Test
    void adminRoutesAllowAdminAndUseAuthenticatedIdentity() {
        Authentication auth = login("admin@example.com", "ROLE_ADMIN");
        SystemUser admin = new SystemUser();
        UUID claimId = UUID.randomUUID();
        when(userService.findByEmail("admin@example.com")).thenReturn(admin);

        adminController.approve(claimId, auth);
        adminController.reject(claimId, new ClaimRejectionRequestDTO("Motivo"), auth);
        adminController.reject(claimId, null, auth);

        verify(claimService).approve(claimId, admin);
        verify(claimService).reject(claimId, "Motivo", admin);
        verify(claimService).reject(claimId, null, admin);
    }

    @Test
    void companyClaimRoutesRefuseCommonUser() {
        login("user@example.com", "ROLE_USER");
        EstablishmentClaimRequestDTO dto = new EstablishmentClaimRequestDTO(UUID.randomUUID(), "Mensagem válida", "88999990000");

        assertThrows(AccessDeniedException.class, () -> companyController.createClaim(dto));
        assertThrows(AccessDeniedException.class, () -> companyController.listMyClaims());
        assertThrows(AccessDeniedException.class, () -> companyController.cancelClaim(UUID.randomUUID()));
        verifyNoInteractions(claimService);
    }

    @Test
    void companyClaimRoutesRefuseAdmin() {
        login("admin@example.com", "ROLE_ADMIN");

        assertThrows(AccessDeniedException.class, () -> companyController.listMyClaims());
        verifyNoInteractions(claimService);
    }

    @Test
    void companyClaimRoutesUseIdentityFromSecurityContext() {
        login("empresa@example.com", "ROLE_EMPRESA");
        SystemUser company = new SystemUser();
        UUID claimId = UUID.randomUUID();
        EstablishmentClaimRequestDTO dto = new EstablishmentClaimRequestDTO(UUID.randomUUID(), "Mensagem válida", "88999990000");
        when(userService.findByEmail("empresa@example.com")).thenReturn(company);

        var created = companyController.createClaim(dto);
        companyController.listMyClaims();
        var cancelled = companyController.cancelClaim(claimId);

        assertEquals(201, created.getStatusCode().value());
        assertEquals(204, cancelled.getStatusCode().value());
        verify(claimService).create(company, dto);
        verify(claimService).listMine(company);
        verify(claimService).cancel(company, claimId);
    }

    private Authentication login(String email, String role) {
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                email, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfiguration {

        @Bean
        EstablishmentClaimService claimService() {
            return mock(EstablishmentClaimService.class);
        }

        @Bean
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        CompanyService companyService() {
            return mock(CompanyService.class);
        }

        @Bean
        EstablishmentClaimController establishmentClaimController(EstablishmentClaimService claimService, UserService userService) {
            return new EstablishmentClaimController(claimService, userService);
        }

        @Bean
        CompanyController companyController(CompanyService companyService, UserService userService, EstablishmentClaimService claimService) {
            return new CompanyController(companyService, userService, claimService);
        }
    }
}
