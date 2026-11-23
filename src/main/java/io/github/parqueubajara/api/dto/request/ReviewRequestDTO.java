package io.github.parqueubajara.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequestDTO(
        @NotNull(message = "A nota é obrigatória")
        @Min(value = 1, message = "A nota deve ser de 1 a 5")
        @Max(value = 5, message = "A nota deve ser de 1 a 5")
        Integer rating,

        @Size(max = 600, message = "O comentário deve ter no máximo 600 caracteres")
        String comment
) {
}
