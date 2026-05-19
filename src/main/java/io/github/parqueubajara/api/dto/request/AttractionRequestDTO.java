package io.github.parqueubajara.api.dto.request;

import io.github.parqueubajara.api.model.enums.AttractionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AttractionRequestDTO(
        //Atributos da superclasse
        @NotBlank(message = "O nome é obrigatório") @Size(max = 100) String name,
        @NotBlank(message = "A descrição é obrigatória") String description,
        @NotBlank(message = "O endereço é obrigatório") String address,
        String phone,

        @Email(message = "E-mail inválido") String email,

        String webUrl,
        String instagramUrl,
        String mapsUrl,

        @NotNull(message = "O status ativo deve ser informado") Boolean active,

        //Atributos da classe
        @Size(max = 200) String shortDescription,
        Boolean openToPublic,
        Boolean freeAccess,
        String openingHours,

        @Positive(message = "O valor informado deve ser positivo") BigDecimal entryPrice,
        Boolean hasGuide,

        Integer averageVisitDuration,
        @NotNull(message = "A categoria deve ser informada") AttractionType category
) {
}
