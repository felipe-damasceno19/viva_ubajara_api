package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.CompanyActivationRequestDTO;
import io.github.parqueubajara.api.dto.response.AuthResponseDTO;
import io.github.parqueubajara.api.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AuthControllerActivationSecurityTest.TestConfiguration.class)
class AuthControllerActivationSecurityTest {

    @Autowired private AuthController controller;
    @Autowired private AuthService authService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void activationIsRejectedWithoutAuthentication() {
        CompanyActivationRequestDTO request =
                new CompanyActivationRequestDTO("Empresa Exemplo", "11222333000181");

        assertThrows(AuthenticationException.class, () -> controller.activateCompany(request, null));
        verifyNoInteractions(authService);
    }

    @Test
    void activationUsesAuthenticatedPrincipalRatherThanRequestIdentity() {
        String authenticatedEmail = "person@example.com";
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                authenticatedEmail, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CompanyActivationRequestDTO request =
                new CompanyActivationRequestDTO("Empresa Exemplo", "11222333000181");
        AuthResponseDTO expected = new AuthResponseDTO(
                "token", authenticatedEmail, "EMPRESA", "Pessoa Exemplo", null);
        when(authService.activateCompany(authenticatedEmail, request)).thenReturn(expected);

        var response = controller.activateCompany(request, authentication);

        assertEquals(expected, response.getBody());
        verify(authService).activateCompany(authenticatedEmail, request);
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfiguration {

        @Bean
        AuthService authService() {
            return mock(AuthService.class);
        }

        @Bean
        AuthController authController(AuthService authService) {
            return new AuthController(authService);
        }
    }
}
