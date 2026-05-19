package io.github.parqueubajara.api.dto.response;

import java.util.List;
import java.util.UUID;

public record RecommendedItemResponseDTO(
        UUID id,
        String name,
        String description,
        String shortDescription,
        String address,
        String webUrl,
        String mapsUrl,
        Boolean active,
        String category,
        Boolean featured,
        List<PhotoResponseDTO> photos
) {
}
