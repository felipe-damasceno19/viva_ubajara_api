package io.github.parqueubajara.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyActivationRequestDTO(
        @NotBlank(message = "Nome da empresa é obrigatório")
        @Size(max = 150, message = "Nome da empresa deve ter no máximo 150 caracteres")
        String companyName,

        @NotBlank(message = "CNPJ é obrigatório")
        @Pattern(
                regexp = "(?:\\d{14}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}|[A-Za-z0-9]{12}\\d{2})",
                message = "CNPJ inválido"
        )
        String cnpj
) {
}
