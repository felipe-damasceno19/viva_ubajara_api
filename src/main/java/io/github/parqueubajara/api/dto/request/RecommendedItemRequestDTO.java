package io.github.parqueubajara.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecommendedItemRequestDTO(
        @NotBlank(message = "O título é obrigatório") @Size(max = 100) String name,
        @NotBlank(message = "A descrição é obrigatória") String description,
        @Size(max = 200) String shortDescription,
        String address,
        String webUrl,
        String mapsUrl,
        @NotBlank(message = "A categoria é obrigatória") String category,
        Boolean featured,
        @NotNull(message = "O status ativo deve ser informado") Boolean active
) {
}
