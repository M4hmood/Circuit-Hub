package com.tekup.circuithub.utils;

import com.tekup.circuithub.models.Product;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    private static final String UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/124.0.0.0 Safari/537.36";

    private static final ExecutorService POOL = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "image-loader");
        t.setDaemon(true);
        return t;
    });

    /**
     * Asynchronously loads a product's image into an ImageView. The view
     * shows a placeholder immediately; the real image swaps in on the FX
     * thread once the bytes are decoded (or the URL is fetched).
     */
    public static void setForProductAsync(ImageView view, Product p, double width, double height) {
        view.setImage(placeholder());
        if (p == null) return;
        POOL.submit(() -> {
            Image img = forProduct(p, width, height);
            Platform.runLater(() -> view.setImage(img));
        });
    }

    /**
     * Asynchronously loads an image from a URL into an ImageView. Returns
     * immediately with a placeholder; the real image swaps in on success.
     */
    public static void setUrlAsync(ImageView view, String url, double width, double height) {
        view.setImage(placeholder());
        if (url == null || url.isBlank()) return;
        POOL.submit(() -> {
            Image img = load(url, width, height);
            Platform.runLater(() -> view.setImage(img));
        });
    }

    /**
     * Synchronous variant — prefers bytes from the DB, falls back to URL,
     * then to a transparent placeholder. Blocks the calling thread, so
     * callers on the FX thread should prefer the async overload above.
     */
    public static Image forProduct(Product p, double width, double height) {
        if (p == null) return placeholder();
        if (p.getImageData() != null && p.getImageData().length > 0) {
            try {
                return new Image(new ByteArrayInputStream(p.getImageData()), width, height, true, true);
            } catch (Exception e) {
                System.err.println("ImageLoader: bad image bytes for " + p.getId() + " — " + e.getMessage());
            }
        }
        return load(p.getImageUrl(), width, height);
    }

    /**
     * Synchronous URL fetch with a browser-like User-Agent. Returns a
     * placeholder on any error.
     */
    public static Image load(String url, double width, double height) {
        if (url == null || url.isBlank()) return placeholder();
        try {
            HttpURLConnection conn = (HttpURLConnection)
                    URI.create(url).toURL().openConnection();
            conn.setRequestProperty("User-Agent", UA);
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(10_000);
            conn.connect();
            if (conn.getResponseCode() != 200) return placeholder();
            try (InputStream in = conn.getInputStream()) {
                return new Image(in, width, height, true, true);
            }
        } catch (Exception e) {
            System.err.println("ImageLoader: failed to load " + url + " — " + e.getMessage());
            return placeholder();
        }
    }

    private static Image placeholder() {
        return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=");
    }
}
