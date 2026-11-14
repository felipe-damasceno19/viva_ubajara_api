package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.CompanyRegisterRequestDTO;
import io.github.parqueubajara.api.dto.request.CompanyActivationRequestDTO;
import io.github.parqueubajara.api.dto.request.LoginRequestDTO;
import io.github.parqueubajara.api.dto.request.UserRequestDTO;
import io.github.parqueubajara.api.dto.response.AuthResponseDTO;
import io.github.parqueubajara.api.handler.StandardError;
import io.github.parqueubajara.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints de autenticação")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar novo usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já cadastrado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid UserRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(requestDTO));
    }

    @Operation(summary = "Registrar nova empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empresa registrada com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já cadastrado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    @PostMapping("/register-company")
    public ResponseEntity<AuthResponseDTO> registerCompany(@RequestBody @Valid CompanyRegisterRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerCompany(requestDTO));
    }

    @Operation(summary = "Ativar acesso empresarial na conta autenticada")
    @PostMapping("/activate-company")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponseDTO> activateCompany(
            @RequestBody @Valid CompanyActivationRequestDTO requestDTO,
            Authentication authentication) {
        return ResponseEntity.ok(authService.activateCompany(authentication.getName(), requestDTO));
    }

    @Operation(summary = "Login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Email ou senha inválidos",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO data){
        return ResponseEntity.ok(authService.login(data));
    }


}
