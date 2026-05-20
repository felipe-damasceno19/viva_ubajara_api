package io.github.parqueubajara.api.dto.request;

import io.github.parqueubajara.api.model.enums.HostType;
import jakarta.validation.constraints.*;

public record HostPointRequestDTO(
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
        @NotNull(message = "Tipo de hospedagem obrigatório!") HostType hostType,

        Integer numOfRooms,

        String avgPrice,

        String bookingUrl
) {
}
