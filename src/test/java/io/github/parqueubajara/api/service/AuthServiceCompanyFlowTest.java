package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.request.CompanyActivationRequestDTO;
import io.github.parqueubajara.api.dto.request.CompanyRegisterRequestDTO;
import io.github.parqueubajara.api.dto.request.LoginRequestDTO;
import io.github.parqueubajara.api.dto.response.AuthResponseDTO;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.enums.Role;
import io.github.parqueubajara.api.security.CustomUserDetailsService;
import io.github.parqueubajara.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceCompanyFlowTest {

    @Mock private UserService userService;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder encoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CustomUserDetailsService customUserDetailsService;
    @InjectMocks private AuthService service;

    @Test
    void newCompanyRegistrationStillCreatesCompanyUser() {
        CompanyRegisterRequestDTO request = new CompanyRegisterRequestDTO(
                "Pessoa", "Exemplo", "new-company", "new@example.com",
                "password1", "Empresa Nova", "11.222.333/0001-81");
        when(encoder.encode("password1")).thenReturn("encoded-password");
        when(userService.existsByCnpj("11222333000181")).thenReturn(false);
        when(customUserDetailsService.loadUserByUsername("new@example.com"))
                .thenReturn(userDetails("new@example.com", "EMPRESA"));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponseDTO response = service.registerCompany(request);

        assertEquals("EMPRESA", response.role());
        assertEquals("new@example.com", response.email());
        verify(userService).save(argThat(user ->
                user.getUserRole() == Role.EMPRESA
                        && "11222333000181".equals(user.getCnpj())
                        && "Empresa Nova".equals(user.getCompanyName())));
    }

    @Test
    void activationUsesAuthenticatedEmailAndReturnsCompanyRole() {
        SystemUser user = new SystemUser();
        user.setEmail("person@example.com");
        user.setFirstName("Pessoa");
        user.setLastName("Exemplo");
        user.setUserRole(Role.EMPRESA);
        user.setCompanyName("Empresa Exemplo");
        user.setCnpj("11222333000181");
        CompanyActivationRequestDTO request =
                new CompanyActivationRequestDTO("Empresa Exemplo", "11222333000181");
        when(userService.activateCompany("person@example.com", "Empresa Exemplo", "11222333000181"))
                .thenReturn(user);
        when(customUserDetailsService.loadUserByUsername("person@example.com"))
                .thenReturn(userDetails("person@example.com", "EMPRESA"));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("new-jwt-token");

        AuthResponseDTO response = service.activateCompany("person@example.com", request);

        assertEquals("EMPRESA", response.role());
        assertEquals("person@example.com", response.email());
        assertEquals("new-jwt-token", response.token());
        verify(userService).activateCompany("person@example.com", "Empresa Exemplo", "11222333000181");
        verify(userService, never()).save(any(SystemUser.class));
    }

    @Test
    void existingLoginFlowStillAuthenticatesAndReturnsCurrentRole() {
        SystemUser user = new SystemUser();
        user.setEmail("person@example.com");
        user.setFirstName("Pessoa");
        user.setUserRole(Role.USER);
        when(userService.findByEmail("person@example.com")).thenReturn(user);
        when(customUserDetailsService.loadUserByUsername("person@example.com"))
                .thenReturn(userDetails("person@example.com", "USER"));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("login-token");

        AuthResponseDTO response = service.login(new LoginRequestDTO("person@example.com", "password"));

        assertEquals("USER", response.role());
        assertEquals("login-token", response.token());
        verify(authenticationManager).authenticate(any());
    }

    private UserDetails userDetails(String email, String role) {
        return User.withUsername(email).password("encoded-password").roles(role).build();
    }
}
