package io.github.parqueubajara.api.dto.response;

import java.util.UUID;

public record GastronomyItemResponseDTO(
        UUID id,
        String name,
        String imageUrl,
        Integer displayOrder,
        Boolean active
) {
}
