package com.example.tracnghiem.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class TextUtils {

    private TextUtils() {
    }

    /**
     * Normalize answer for comparison.
     * - For numbers: normalize decimal format (121.0 = 121 = 121,0)
     * - For text: trim, normalize spaces, lowercase, remove Vietnamese diacritics
     */
    public static String normalizeAnswer(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        // Try to parse as number
        try {
            // Replace comma with dot for decimal separator (European format: 121,0 -> 121.0)
            String normalizedNumber = trimmed.replace(',', '.');
            double number = Double.parseDouble(normalizedNumber);
            
            // If it's a whole number, return as integer (121.0 -> 121)
            if (number == Math.floor(number)) {
                return String.valueOf((long) number);
            } else {
                // Has meaningful decimal part, remove trailing zeros
                DecimalFormat df = new DecimalFormat("#.##########", DecimalFormatSymbols.getInstance(Locale.US));
                return df.format(number);
            }
        } catch (NumberFormatException e) {
            // Not a number, normalize as text
            String normalized = trimmed.replaceAll("\\s+", " ").toLowerCase();
            // Remove Vietnamese diacritics for flexible comparison
            normalized = removeVietnameseDiacritics(normalized);
            return normalized;
        }
    }

    /**
     * Remove Vietnamese diacritics (accents) from text.
     * Example: "bảo toàn" -> "bao toan", "điện" -> "dien"
     */
    private static String removeVietnameseDiacritics(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Vietnamese character mappings
        return text
                .replace("à", "a").replace("á", "a").replace("ạ", "a").replace("ả", "a").replace("ã", "a")
                .replace("â", "a").replace("ầ", "a").replace("ấ", "a").replace("ậ", "a").replace("ẩ", "a").replace("ẫ", "a")
                .replace("ă", "a").replace("ằ", "a").replace("ắ", "a").replace("ặ", "a").replace("ẳ", "a").replace("ẵ", "a")
                .replace("è", "e").replace("é", "e").replace("ẹ", "e").replace("ẻ", "e").replace("ẽ", "e")
                .replace("ê", "e").replace("ề", "e").replace("ế", "e").replace("ệ", "e").replace("ể", "e").replace("ễ", "e")
                .replace("ì", "i").replace("í", "i").replace("ị", "i").replace("ỉ", "i").replace("ĩ", "i")
                .replace("ò", "o").replace("ó", "o").replace("ọ", "o").replace("ỏ", "o").replace("õ", "o")
                .replace("ô", "o").replace("ồ", "o").replace("ố", "o").replace("ộ", "o").replace("ổ", "o").replace("ỗ", "o")
                .replace("ơ", "o").replace("ờ", "o").replace("ớ", "o").replace("ợ", "o").replace("ở", "o").replace("ỡ", "o")
                .replace("ù", "u").replace("ú", "u").replace("ụ", "u").replace("ủ", "u").replace("ũ", "u")
                .replace("ư", "u").replace("ừ", "u").replace("ứ", "u").replace("ự", "u").replace("ử", "u").replace("ữ", "u")
                .replace("ỳ", "y").replace("ý", "y").replace("ỵ", "y").replace("ỷ", "y").replace("ỹ", "y")
                .replace("đ", "d")
                .replace("À", "a").replace("Á", "a").replace("Ạ", "a").replace("Ả", "a").replace("Ã", "a")
                .replace("Â", "a").replace("Ầ", "a").replace("Ấ", "a").replace("Ậ", "a").replace("Ẩ", "a").replace("Ẫ", "a")
                .replace("Ă", "a").replace("Ằ", "a").replace("Ắ", "a").replace("Ặ", "a").replace("Ẳ", "a").replace("Ẵ", "a")
                .replace("È", "e").replace("É", "e").replace("Ẹ", "e").replace("Ẻ", "e").replace("Ẽ", "e")
                .replace("Ê", "e").replace("Ề", "e").replace("Ế", "e").replace("Ệ", "e").replace("Ể", "e").replace("Ễ", "e")
                .replace("Ì", "i").replace("Í", "i").replace("Ị", "i").replace("Ỉ", "i").replace("Ĩ", "i")
                .replace("Ò", "o").replace("Ó", "o").replace("Ọ", "o").replace("Ỏ", "o").replace("Õ", "o")
                .replace("Ô", "o").replace("Ồ", "o").replace("Ố", "o").replace("Ộ", "o").replace("Ổ", "o").replace("Ỗ", "o")
                .replace("Ơ", "o").replace("Ờ", "o").replace("Ớ", "o").replace("Ợ", "o").replace("Ở", "o").replace("Ỡ", "o")
                .replace("Ù", "u").replace("Ú", "u").replace("Ụ", "u").replace("Ủ", "u").replace("Ũ", "u")
                .replace("Ư", "u").replace("Ừ", "u").replace("Ứ", "u").replace("Ự", "u").replace("Ử", "u").replace("Ữ", "u")
                .replace("Ỳ", "y").replace("Ý", "y").replace("Ỵ", "y").replace("Ỷ", "y").replace("Ỹ", "y")
                .replace("Đ", "d");
    }
}

