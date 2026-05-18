package io.github.parqueubajara.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactMessageRequestDTO(

        @NotBlank(message = "Name is required") @Size(max = 100) String name,

        @NotBlank(message = "Email is required") @Email(message = "Invalid email") String email,

        @Size(max = 150) String subject,

        @NotBlank(message = "Message is required") @Size(max = 2000) String message

) {
}
