package com.bobeat.backend.global.util;

public final class KeysetCursor {
    private KeysetCursor() {}

    public record PageCursor(int distance, long id) {}

    /** "distance:id" 로 인코딩 */
    public static String encode(int distance, long id) {
        return distance + ":" + id;
    }

    /** "distance:id" → PageCursor 디코딩 (실패 시 null) */
    public static PageCursor decodeOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            String[] parts = s.split(":");
            if (parts.length != 2) return null;
            int d = Integer.parseInt(parts[0]);
            long i = Long.parseLong(parts[1]);
            return new PageCursor(d, i);
        } catch (Exception e) {
            return null;
        }
    }
}