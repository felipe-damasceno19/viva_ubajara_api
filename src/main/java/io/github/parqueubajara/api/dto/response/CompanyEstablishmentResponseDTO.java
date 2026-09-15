package io.github.parqueubajara.api.dto.response;

import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import io.github.parqueubajara.api.model.enums.EstablishmentType;
import io.github.parqueubajara.api.model.enums.HostType;

import java.util.List;
import java.util.UUID;

public record CompanyEstablishmentResponseDTO(
        UUID id,
        EstablishmentType type,
        String name,
        String description,
        String address,
        String phone,
        String email,
        String webUrl,
        String instagramUrl,
        String mapsUrl,
        Boolean active,
        ApprovalStatus approvalStatus,

        //Atributos específicos de Restaurant
        String cuisineType,
        String openingHours,
        String avgPrice,
        Boolean acceptsReservation,
        Integer starRating,

        //Atributos específicos de HostPoint
        HostType hostType,
        Integer numOfRooms,
        String bookingUrl,

        List<PhotoResponseDTO> photos
) {
}
