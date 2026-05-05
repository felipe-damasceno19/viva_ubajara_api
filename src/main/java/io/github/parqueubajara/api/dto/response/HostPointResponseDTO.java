package io.github.parqueubajara.api.dto.response;

import io.github.parqueubajara.api.model.enums.HostType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record HostPointResponseDTO(
        UUID id,
        String name,
        String description,
        String address,
        String phone,
        String email,
        String webUrl,
        String instagramUrl,
        Boolean active,
        HostType hostType,
        Integer numOfRooms,
        BigDecimal avgPrice,
        String bookingUrl,
        List<PhotoResponseDTO> photos
) {
}
