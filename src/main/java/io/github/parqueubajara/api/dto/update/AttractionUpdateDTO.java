package io.github.parqueubajara.api.dto.update;

import io.github.parqueubajara.api.model.enums.AttractionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AttractionUpdateDTO(
        @Size(max = 100) String name,
        @Size(max = 300) String description,
        @Size(max = 100) String address,
        String phone,

        @Email(message = "E-mail inválido") String email,

        String webUrl,
        String instagramUrl,
        Boolean active,

        //Atributos da classe
        @Size(max = 200) String shortDescription,
        Boolean openToPublic,
        Boolean freeAccess,
        String openingHours,

        @Positive(message = "O valor informado deve ser positivo") BigDecimal entryPrice,
        Boolean hasGuide,

        Integer averageVisitDuration,
        AttractionType category
) {
}
