package com.bobeat.backend.domain.store.util;

import java.util.List;
import java.util.stream.Collectors;

public class PgVectorUtils {
    public static String toLiteral(List<Float> vector) {
        return "[" + vector.stream()
                .map(d -> String.format("%.6f", d))
                .collect(Collectors.joining(",")) + "]";
    }
}