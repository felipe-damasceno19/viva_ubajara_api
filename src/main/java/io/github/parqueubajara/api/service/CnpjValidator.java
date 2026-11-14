package io.github.parqueubajara.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CnpjValidator {

    private static final int[] FIRST_DIGIT_WEIGHTS = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] SECOND_DIGIT_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private CnpjValidator() {
    }

    public static String normalize(String value) {
        String formattedValue = value.toUpperCase(Locale.ROOT);
        if (!formattedValue.matches(
                "(?:\\d{14}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}|[A-Z0-9]{12}\\d{2})")) {
            throw new IllegalArgumentException("CNPJ inválido");
        }

        String cnpj = formattedValue.replaceAll("[./-]", "");
        if (!cnpj.matches("(?:\\d{14}|[A-Z0-9]{12}\\d{2})")
                || cnpj.chars().distinct().count() == 1
                || !hasValidCheckDigits(cnpj)) {
            throw new IllegalArgumentException("CNPJ inválido");
        }
        return cnpj;
    }

    public static List<String> equivalentValues(String normalizedCnpj) {
        List<String> values = new ArrayList<>();
        values.add(normalizedCnpj);
        if (normalizedCnpj.matches("\\d{14}")) {
            values.add(normalizedCnpj.substring(0, 2) + "."
                    + normalizedCnpj.substring(2, 5) + "."
                    + normalizedCnpj.substring(5, 8) + "/"
                    + normalizedCnpj.substring(8, 12) + "-"
                    + normalizedCnpj.substring(12));
        } else {
            values.add(normalizedCnpj.toLowerCase(Locale.ROOT));
        }
        return values;
    }

    private static boolean hasValidCheckDigits(String cnpj) {
        int firstDigit = calculateCheckDigit(cnpj.substring(0, 12), FIRST_DIGIT_WEIGHTS);
        if (firstDigit != Character.digit(cnpj.charAt(12), 10)) {
            return false;
        }

        int secondDigit = calculateCheckDigit(cnpj.substring(0, 13), SECOND_DIGIT_WEIGHTS);
        return secondDigit == Character.digit(cnpj.charAt(13), 10);
    }

    private static int calculateCheckDigit(String value, int[] weights) {
        int sum = 0;
        for (int index = 0; index < weights.length; index++) {
            sum += cnpjCharacterValue(value.charAt(index)) * weights[index];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static int cnpjCharacterValue(char character) {
        return Character.isDigit(character) ? character - '0' : character - 48;
    }
}
