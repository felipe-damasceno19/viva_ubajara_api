package io.github.parqueubajara.api.dto.response;

import io.github.parqueubajara.api.model.enums.AttractionType;

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
        String mapsUrl,
        Boolean active,
        String shortDescription,
        Boolean openToPublic,
        Boolean freeAccess,
        String openingHours,
        String entryPrice,
        Boolean hasGuide,
        Integer averageVisitDuration,
        AttractionType category,
        List<PhotoResponseDTO> photos,
        List<AttractionResponseDTO> subAttractions,
        List<TouristSpotSummaryDTO> linkedSpots
) {
}
