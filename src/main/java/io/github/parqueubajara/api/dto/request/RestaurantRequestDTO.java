package io.github.parqueubajara.api.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RestaurantRequestDTO(
        //Atributos da super classe
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
        @NotBlank(message = "Tipo de culinária do restaurante obrigatório") String cuisineType,

        String openingHours,

        @Positive(message = "O valor informado deve ser positivo") BigDecimal avgPrice,

        Boolean acceptsReservation

) {
}
