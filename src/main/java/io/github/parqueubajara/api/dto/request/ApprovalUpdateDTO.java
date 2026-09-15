package io.github.parqueubajara.api.dto.request;

import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;

public record ApprovalUpdateDTO(
        @NotNull(message = "O status de aprovação é obrigatório") ApprovalStatus status
) {
}
