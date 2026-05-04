package io.github.parqueubajara.api.service;

import io.github.parqueubajara.api.dto.request.LoginRequestDTO;
import io.github.parqueubajara.api.dto.request.UserRequestDTO;
import io.github.parqueubajara.api.dto.response.AuthResponseDTO;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.enums.Role;
import io.github.parqueubajara.api.security.CustomUserDetailsService;
import io.github.parqueubajara.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthResponseDTO register(UserRequestDTO requestDTO){

        SystemUser user = new SystemUser();
        user.setFirstName(requestDTO.firstName());
        user.setLastName(requestDTO.lastName());
        user.setUsername(requestDTO.username());
        user.setEmail(requestDTO.email());
        user.setPassword(encoder.encode(requestDTO.password()));
        user.setUserRole(Role.ADMIN);

        userService.save(user);

        UserDetails userDetails = customUserDetailsService
                .loadUserByUsername(user.getEmail());

        String token = jwtService.generateToken(userDetails);

        return new AuthResponseDTO(token, user.getEmail(), user.getUserRole().name());
    }

    public AuthResponseDTO login(LoginRequestDTO requestDTO){

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDTO.email(), requestDTO.password()
                )
        );

        SystemUser user = userService.findByEmail(requestDTO.email());

        UserDetails userDetails = customUserDetailsService
                .loadUserByUsername(user.getEmail());

        String token = jwtService.generateToken(userDetails);

        return new AuthResponseDTO(token, user.getEmail(), user.getUserRole().name());
    }

    @Transactional
    public SystemUser processSocialLogin(String email, String firstName, String lastName){
        return userService.findByEmailOptional(email)
                .orElseGet(() -> {
                    SystemUser newUser = new SystemUser();
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setEmail(email);

                    String baseUsername = email.split("@")[0];
                    String finalUsername = baseUsername;

                    if(userService.existsByUsername(finalUsername)){
                        finalUsername = baseUsername + UUID.randomUUID().toString();
                    }

                    newUser.setUsername(finalUsername);
                    newUser.setPassword(encoder.encode(UUID.randomUUID().toString()));
                    newUser.setUserRole(Role.USER);

                    return userService.save(newUser);
                });
    }
}
