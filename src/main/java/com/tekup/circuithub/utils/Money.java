package com.tekup.circuithub.utils;

public final class Money {
    public static final String CURRENCY = "TND";

    private Money() {}

    /** "12.50 TND" */
    public static String format(double v)  { return String.format("%.2f %s", v, CURRENCY); }
    /** "13 TND" — used by sliders */
    public static String formatRound(double v) { return String.format("%.0f %s", v, CURRENCY); }
    /** "12.50 TND each" */
    public static String formatEach(double v) { return format(v) + " each"; }
}
