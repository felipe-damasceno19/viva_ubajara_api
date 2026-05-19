package io.github.parqueubajara.api.dto.update;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventUpdateDTO(
        @Size(max = 100) String name,

        @Size(max = 300) String description,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
        @Schema(type = "string", pattern = "dd/MM/yyyy HH:mm:ss", example = "25/04/2026 10:00:00")
        LocalDateTime startDateTime,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
        @Schema(type = "string", pattern = "dd/MM/yyyy HH:mm:ss", example = "25/04/2026 10:00:00")
        LocalDateTime endDateTime,

        @Size(max = 100) String location,

        String registrationUrl,

        Boolean active
) {
}
