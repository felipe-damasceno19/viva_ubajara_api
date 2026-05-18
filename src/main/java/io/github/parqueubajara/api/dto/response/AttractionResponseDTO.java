package io.github.parqueubajara.api.dto.response;

import io.github.parqueubajara.api.model.enums.AttractionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AttractionResponseDTO(
        UUID id,
        String name,
        String description,
        String address,
        String phone,
        String email,
        String webUrl,
        String instagramUrl,
        Boolean active,
        String openingHours,
        BigDecimal entryPrice,
        Boolean hasGuide,
        Integer averageVisitDuration,
        AttractionType category,
        List<PhotoResponseDTO> photos,
        List<AttractionResponseDTO> subAttractions
) {
}
