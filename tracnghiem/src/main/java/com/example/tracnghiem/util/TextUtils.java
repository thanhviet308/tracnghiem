package com.example.tracnghiem.util;

public final class TextUtils {

    private TextUtils() {
    }

    public static String normalizeAnswer(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}

