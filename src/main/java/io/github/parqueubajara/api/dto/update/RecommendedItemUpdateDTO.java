package io.github.parqueubajara.api.dto.update;

import jakarta.validation.constraints.Size;

public record RecommendedItemUpdateDTO(
        @Size(max = 100) String name,
        String description,
        @Size(max = 200) String shortDescription,
        String address,
        String webUrl,
        String mapsUrl,
        String category,
        Boolean featured,
        Boolean active
) {
}
