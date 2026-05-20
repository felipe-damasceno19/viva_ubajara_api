package io.github.parqueubajara.api.dto.update;

import io.github.parqueubajara.api.model.enums.AttractionType;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public record AttractionUpdateDTO(
        @Size(max = 100) String name,
        @Size(max = 300) String description,
        @Size(max = 100) String address,
        String phone,

        @Email(message = "E-mail inválido") String email,

        String webUrl,
        String instagramUrl,
        String mapsUrl,
        Boolean active,

        //Atributos da classe
        @Size(max = 200) String shortDescription,
        Boolean openToPublic,
        Boolean freeAccess,
        String openingHours,

        String entryPrice,
        Boolean hasGuide,

        Integer averageVisitDuration,
        AttractionType category,
        List<UUID> linkedSpotIds
) {
}
