package io.github.parqueubajara.api.dto.update;

import io.github.parqueubajara.api.model.enums.HostType;
import jakarta.validation.constraints.*;

public record CompanyEstablishmentUpdateDTO(
        //Atributos da super classe
        @Size(max = 100) String name,

        @Size(max = 300) String description,

        @Size(max = 100) String address,

        String phone,

        @Email(message = "E-mail inválido") String email,

        String webUrl,
        String instagramUrl,
        String mapsUrl,

        //Atributos específicos de Restaurant
        String cuisineType,

        String openingHours,

        String avgPrice,

        Boolean acceptsReservation,

        Integer starRating,

        //Atributos específicos de HostPoint
        HostType hostType,

        Integer numOfRooms,

        String bookingUrl
) {
}
