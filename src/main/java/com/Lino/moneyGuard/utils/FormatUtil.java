package com.Lino.moneyGuard.utils;

import java.text.DecimalFormat;

public class FormatUtil {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat SHORT_FORMAT = new DecimalFormat("#,##0");

    public static String formatMoney(double amount) {
        if (amount >= 1000000) {
            return SHORT_FORMAT.format(amount / 1000000) + "M";
        } else if (amount >= 1000) {
            return SHORT_FORMAT.format(amount / 1000) + "K";
        }
        return MONEY_FORMAT.format(amount);
    }

    public static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        }
        return seconds + "s";
    }
}