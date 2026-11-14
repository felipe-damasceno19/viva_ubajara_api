package io.github.parqueubajara.api.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CnpjValidatorTest {

    @Test
    void normalizesFormattedNumericCnpj() {
        assertEquals("11222333000181", CnpjValidator.normalize("11.222.333/0001-81"));
    }

    @Test
    void rejectsCnpjWithInvalidCheckDigits() {
        assertThrows(IllegalArgumentException.class,
                () -> CnpjValidator.normalize("11222333000182"));
    }
}
