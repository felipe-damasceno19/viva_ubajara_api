package io.github.parqueubajara.api.dto.response;

import java.util.Map;

public record ReviewSummaryDTO(
        Double average,
        long count,
        Map<Integer, Long> distribution
) {
}
