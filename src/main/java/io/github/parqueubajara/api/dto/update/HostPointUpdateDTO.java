package io.github.parqueubajara.api.dto.update;

import io.github.parqueubajara.api.model.enums.HostType;
import jakarta.validation.constraints.*;

public record HostPointUpdateDTO(
        //Atributos da superclasse
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
        HostType hostType,

        Integer numOfRooms,

        String avgPrice,

        String bookingUrl
) {
}
