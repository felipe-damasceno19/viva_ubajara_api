package io.github.parqueubajara.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record EstablishmentClaimRequestDTO(
        @NotNull(message = "Estabelecimento é obrigatório")
        UUID spotId,

        @NotBlank(message = "Mensagem é obrigatória")
        @Size(min = 10, max = 1000, message = "Mensagem deve ter entre 10 e 1000 caracteres")
        String message,

        @NotBlank(message = "Telefone de contato é obrigatório")
        @Size(max = 30, message = "Telefone de contato deve ter no máximo 30 caracteres")
        @Pattern(regexp = "[0-9\\s()+\\-]*", message = "Telefone de contato inválido")
        String contactPhone
) {
}
