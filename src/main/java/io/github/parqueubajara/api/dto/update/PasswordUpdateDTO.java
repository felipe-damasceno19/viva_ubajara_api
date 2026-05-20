package io.github.parqueubajara.api.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateDTO(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres") String newPassword
) {
}
