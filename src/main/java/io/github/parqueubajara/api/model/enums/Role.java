package io.github.parqueubajara.api.model.enums;


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
}
