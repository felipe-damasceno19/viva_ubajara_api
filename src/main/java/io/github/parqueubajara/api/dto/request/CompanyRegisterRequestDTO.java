package io.github.parqueubajara.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRegisterRequestDTO(
        @NotBlank(message = "Campo obrigatório!")
        @Size(min = 2, max = 20, message = "Limite máximo de 20 caracteres!")
        String firstName,

        @Size(max = 20, message = "Limite máximo de 20 caracteres!") String lastName,

        @NotBlank(message = "Preencha o campo de usuário")
        @Size(min = 2, max = 30, message = "Limite máximo de 20 caracteres!")
        String username,

        @NotBlank(message = "Email obrigatório!") @Email String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password,

        @NotBlank(message = "Nome da empresa é obrigatório")
        String companyName,

        @NotBlank(message = "CNPJ é obrigatório")
        String cnpj
) {
}
