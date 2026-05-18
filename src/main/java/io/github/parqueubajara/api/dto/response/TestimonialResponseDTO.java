package io.github.parqueubajara.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestimonialResponseDTO(

        UUID id,
        String userName,
        String userEmail,
        String userPhoto,
        Integer rating,
        String comment,
        Boolean approved,
        LocalDateTime createdDate

) {
}
