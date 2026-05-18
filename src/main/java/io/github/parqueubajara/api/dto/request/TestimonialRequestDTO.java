package io.github.parqueubajara.api.dto.request;

import jakarta.validation.constraints.*;

public record TestimonialRequestDTO(

        @NotBlank(message = "Name is required") @Size(max = 100) String userName,

        String userEmail,

        String userPhoto,

        @NotNull(message = "Rating is required") @Min(1) @Max(5) Integer rating,

        @NotBlank(message = "Comment is required") @Size(max = 600) String comment

) {
}
