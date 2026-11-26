package com.smartmart.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;

final class WaktuUtil {

    private WaktuUtil() { }

    static LocalDateTime parse(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String t = s.trim().replace('T', ' ');
        if (t.length() >= 19) {
            return LocalDateTime.parse(t.substring(0, 19).replace(' ', 'T'));
        }
        if (t.length() >= 16) {
            return LocalDateTime.parse(t.substring(0, 16).replace(' ', 'T'));
        }
        return LocalDate.parse(t.substring(0, 10)).atStartOfDay();
    }

    static boolean bolehRetur(Object nilai) {
        if (nilai == null) return true;
        if (nilai instanceof Number) return ((Number) nilai).intValue() != 0;
        String s = nilai.toString().trim().toLowerCase();
        return s.equals("1") || s.equals("true") || s.equals("ya") || s.startsWith("dapat");
    }
}
