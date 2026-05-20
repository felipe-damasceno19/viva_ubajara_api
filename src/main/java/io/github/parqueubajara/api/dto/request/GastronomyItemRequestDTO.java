package io.github.parqueubajara.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GastronomyItemRequestDTO(
        @NotBlank(message = "O nome é obrigatório") String name,
        Integer displayOrder,
        Boolean active
) {
}
