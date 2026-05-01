package com.tekup.circuithub.utils;

import javafx.scene.text.Font;

import java.net.URL;

/**
 * Loads bundled TTF fonts on startup. Silently skips any font whose TTF is
 * missing — the app falls back to Consolas / Segoe UI via the CSS stack.
 * Drop the TTFs into resources/com/tekup/circuithub/assets/fonts/ using
 * the filenames listed in FILES below.
 */
public class Fonts {
    private static final String BASE = "/com/tekup/circuithub/assets/fonts/";
    private static final String[] FILES = {
            "JetBrainsMono-Regular.ttf",
            "JetBrainsMono-Bold.ttf",
            "Inter-Regular.ttf",
            "Inter-Bold.ttf"
    };

    public static void loadAll() {
        for (String f : FILES) {
            URL url = Fonts.class.getResource(BASE + f);
            if (url == null) continue;
            try {
                Font.loadFont(url.toExternalForm(), 12);
            } catch (Exception ignored) {}
        }
    }
}
