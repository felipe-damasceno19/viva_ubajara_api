package io.github.parqueubajara.api.dto.update;

import io.github.parqueubajara.api.model.enums.Role;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateDTO(@NotNull Role role) {
}
