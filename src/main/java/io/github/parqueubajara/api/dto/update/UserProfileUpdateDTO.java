package io.github.parqueubajara.api.dto.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateDTO(
        @Size(min = 2, max = 20, message = "Limite máximo de 20 caracteres!") String firstName,

        @Size(min = 2, max = 20, message = "Limite máximo de 20 caracteres!") String lastName,

        @Size(min = 2, max = 30, message = "Limite máximo de 30 caracteres!") String username,

        @Email(message = "E-mail inválido") String email,

        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres") String password
) {
}
