package org.tud.mensadresden.model.service.mensa;

import java.util.Locale;

public class Utils {
    public static String formatDistance(float distance, int precision) {
        if (distance < 1000) {
            return Math.round(distance) + " m";
        } else {
            return String.format(Locale.ENGLISH, "%." + precision + "f", distance / 1000) + " km";
        }
    }
}
