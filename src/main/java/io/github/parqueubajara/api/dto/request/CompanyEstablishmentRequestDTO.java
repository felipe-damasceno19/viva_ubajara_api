package io.github.parqueubajara.api.dto.request;

import io.github.parqueubajara.api.model.enums.EstablishmentType;
import io.github.parqueubajara.api.model.enums.HostType;
import jakarta.validation.constraints.*;

public record CompanyEstablishmentRequestDTO(
        @NotNull(message = "O tipo de estabelecimento é obrigatório") EstablishmentType type,

        //Atributos da super classe
        @NotBlank(message = "O nome é obrigatório") @Size(max = 100) String name,

        @NotBlank(message = "A descrição é obrigatória") String description,

        @NotBlank(message = "O endereço é obrigatório") String address,

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
