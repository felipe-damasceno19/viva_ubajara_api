package io.github.parqueubajara.api.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum Role {

    ADMIN("admin"),
    USER("user"),
    GUIDE("guide");

    private final String valor;

    Role(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static Role fromValue(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(r -> r.name().equalsIgnoreCase(value) || r.valor.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role inválida: " + value));
    }
}
