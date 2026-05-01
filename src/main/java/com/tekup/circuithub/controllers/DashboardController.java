package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.Product;
import com.tekup.circuithub.models.User;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.SceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private StackPane bannerPane;
    @FXML private Label bannerTitle;
    @FXML private Label bannerDesc;
    @FXML private Label bannerPrice;
    @FXML private Label bannerCategory;
    @FXML private GridPane categoryGrid;

    private List<Product> featured;
    private int featuredIdx = 0;

    private static final String[][] CATS = {
            {"Microcontrollers", "⚡"},
            {"Sensors",          "◉"},
            {"Displays",         "▣"},
            {"Modules",          "◈"},
            {"Tools",            "✦"},
            {"Power",            "⌁"}
    };

    @FXML
    public void initialize() {
        User u = DataStore.getCurrentUser();
        welcomeLabel.setText("Welcome, " + (u == null ? "there" : u.getFullName()));

        List<Product> all = DataStore.loadProducts();
        buildCategoryCards(all);
        buildFeatured(all);
    }

    private void buildCategoryCards(List<Product> all) {
        Map<String, Long> counts = new java.util.LinkedHashMap<>();
        for (Product p : all) counts.merge(p.getCategory(), 1L, Long::sum);

        for (int i = 0; i < CATS.length; i++) {
            String name = CATS[i][0];
            String icon = CATS[i][1];
            long count = counts.getOrDefault(name, 0L);

            VBox card = new VBox(10);
            card.getStyleClass().add("category-card");
            Label iconLbl = new Label(icon);
            iconLbl.getStyleClass().add("category-icon");
            Label nameLbl = new Label(name);
            nameLbl.getStyleClass().add("category-name");
            Label countLbl = new Label("// " + count + " items in stock");
            countLbl.getStyleClass().add("category-count");
            Region spacer = new Region(); spacer.setPrefHeight(4);
            card.getChildren().addAll(iconLbl, spacer, nameLbl, countLbl);
            card.setPrefHeight(180);

            attachHoverLift(card);
            card.setOnMouseClicked(e ->
                    SceneManager.getInstance().switchTo("products", true));

            categoryGrid.add(card, i % 3, i / 3);
        }
        SceneManager.staggerIn(categoryGrid.getChildren());
    }

    private void attachHoverLift(Node n) {
        TranslateTransition up = new TranslateTransition(Duration.millis(180), n);
        up.setToY(-5);
        TranslateTransition down = new TranslateTransition(Duration.millis(180), n);
        down.setToY(0);
        n.setOnMouseEntered(e -> { down.stop(); up.playFromStart(); });
        n.setOnMouseExited(e -> { up.stop(); down.playFromStart(); });
    }

    private void buildFeatured(List<Product> all) {
        if (all.isEmpty()) return;
        java.util.List<Product> pool = new java.util.ArrayList<>(all);
        java.util.Collections.shuffle(pool, new Random());
        featured = pool.subList(0, Math.min(5, pool.size()));

        showFeatured(0);

        Timeline rotate = new Timeline(new KeyFrame(Duration.seconds(4), e -> {
            featuredIdx = (featuredIdx + 1) % featured.size();
            fadeSwap(() -> showFeatured(featuredIdx));
        }));
        rotate.setCycleCount(Timeline.INDEFINITE);
        rotate.play();
    }

    private void fadeSwap(Runnable swap) {
        FadeTransition out = new FadeTransition(Duration.millis(280), bannerPane);
        out.setFromValue(1.0); out.setToValue(0.15);
        out.setOnFinished(e -> {
            swap.run();
            FadeTransition in = new FadeTransition(Duration.millis(280), bannerPane);
            in.setFromValue(0.15); in.setToValue(1.0);
            in.play();
        });
        out.play();
    }

    private void showFeatured(int i) {
        Product p = featured.get(i);
        bannerTitle.setText(p.getName());
        bannerDesc.setText(p.getDescription());
        bannerPrice.setText(String.format("$%.2f", p.getPrice()));
        bannerCategory.setText(p.getCategory());
    }

    // ---- Navigation ----
    @FXML private void navHome(ActionEvent e)     { /* already here */ }
    @FXML private void navProducts(ActionEvent e) { SceneManager.getInstance().switchTo("products", true); }
    @FXML private void navCart(ActionEvent e)     { SceneManager.getInstance().switchTo("cart", true); }
    @FXML private void navOrders(ActionEvent e)   { SceneManager.getInstance().switchTo("orders", true); }
    @FXML private void navProfile(ActionEvent e)  { SceneManager.getInstance().switchTo("profile", true); }

    @FXML
    private void onLogout(ActionEvent e) {
        DataStore.logout();
        SceneManager.getInstance().switchTo("welcome", true);
    }
}
