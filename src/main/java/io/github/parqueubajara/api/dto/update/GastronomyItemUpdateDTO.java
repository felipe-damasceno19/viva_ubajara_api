package io.github.parqueubajara.api.dto.update;

public record GastronomyItemUpdateDTO(
        String name,
        Integer displayOrder,
        Boolean active
) {
}
