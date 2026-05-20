package io.github.parqueubajara.api.dto.update;

import jakarta.validation.constraints.*;

public record RestaurantUpdateDTO(
        //Atributos da super classe
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
        String cuisineType,

        String openingHours,

        String avgPrice,

        Boolean acceptsReservation,

        Integer starRating
) {
}
