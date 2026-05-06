package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.Product;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.ImageLoader;
import com.tekup.circuithub.utils.SceneManager;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class ProductsController {

    @FXML private ComboBox<String> categoryCombo;
    @FXML private Slider priceSlider;
    @FXML private Label priceLabel;
    @FXML private TextField searchField;
    @FXML private FlowPane productsPane;
    @FXML private Label resultCount;

    private List<Product> all;

    @FXML
    public void initialize() {
        all = DataStore.loadProducts();

        categoryCombo.setItems(FXCollections.observableArrayList(
                "All", "Microcontrollers", "Sensors", "Displays", "Modules", "Tools", "Power"));
        categoryCombo.getSelectionModel().select("All");
        categoryCombo.valueProperty().addListener((o, a, b) -> render());

        double max = all.stream().mapToDouble(Product::getPrice).max().orElse(200);
        priceSlider.setMax(Math.ceil(max));
        priceSlider.setValue(Math.ceil(max));
        priceLabel.setText(String.format("$%.0f", priceSlider.getValue()));
        priceSlider.valueProperty().addListener((o, a, b) -> {
            priceLabel.setText(String.format("$%.0f", b.doubleValue()));
            render();
        });

        searchField.textProperty().addListener((o, a, b) -> render());

        render();
    }

    private void render() {
        String cat = categoryCombo.getValue();
        double maxP = priceSlider.getValue();
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<Product> filtered = all.stream()
                .filter(p -> "All".equals(cat) || cat == null || p.getCategory().equals(cat))
                .filter(p -> p.getPrice() <= maxP)
                .filter(p -> q.isEmpty()
                        || p.getName().toLowerCase().contains(q)
                        || p.getCategory().toLowerCase().contains(q)
                        || p.getSpecs().values().stream().anyMatch(v -> v.toLowerCase().contains(q)))
                .collect(Collectors.toList());

        resultCount.setText("// " + filtered.size() + " components");
        productsPane.getChildren().clear();
        for (Product p : filtered) productsPane.getChildren().add(buildCard(p));
        SceneManager.staggerIn(productsPane.getChildren());
    }

    private VBox buildCard(Product p) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(260);
        card.setPrefHeight(320);
        card.setMaxWidth(260);

        ImageView iv = new ImageView();
        iv.setFitWidth(232);
        iv.setFitHeight(140);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        ImageLoader.setForProductAsync(iv, p, 192, 130);
        VBox thumb = new VBox(iv);
        thumb.getStyleClass().add("product-thumb");
        thumb.setPrefHeight(140);
        thumb.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-border-radius: 10; -fx-alignment: center; -fx-padding: 8;");

        Label cat = new Label(p.getCategory().toUpperCase());
        cat.getStyleClass().add("chip-category");

        Label name = new Label(p.getName());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);

        String firstSpec = p.getSpecs().isEmpty() ? ""
                : p.getSpecs().entrySet().iterator().next().toString();
        Label specs = new Label(firstSpec);
        specs.getStyleClass().add("product-specs");
        specs.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox priceRow = new HBox();
        priceRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label price = new Label(String.format("$%.2f", p.getPrice()));
        price.getStyleClass().add("price");
        price.setStyle("-fx-font-size: 18px;");
        Region gap = new Region();
        HBox.setHgrow(gap, javafx.scene.layout.Priority.ALWAYS);
        Button add = new Button("+ CART");
        add.getStyleClass().add("btn-add");
        add.setOnAction(e -> {
            DataStore.addToCart(p, 1);
            add.setText("✓ ADDED");
            javafx.animation.PauseTransition pt =
                    new javafx.animation.PauseTransition(javafx.util.Duration.millis(900));
            pt.setOnFinished(x -> add.setText("+ CART"));
            pt.play();
            e.consume();
        });
        priceRow.getChildren().addAll(price, gap, add);

        card.getChildren().addAll(thumb, cat, name, specs, spacer, priceRow);

        TranslateTransition liftUp   = new TranslateTransition(Duration.millis(160), card);
        liftUp.setToY(-6);
        TranslateTransition liftDown = new TranslateTransition(Duration.millis(160), card);
        liftDown.setToY(0);
        card.setOnMouseEntered(e -> { liftDown.stop(); liftUp.playFromStart(); });
        card.setOnMouseExited(e ->  { liftUp.stop(); liftDown.playFromStart(); });

        card.setOnMouseClicked(e -> {
            DataStore.setSelectedProduct(p);
            SceneManager.getInstance().switchTo("product-detail", true);
        });
        return card;
    }

    @FXML private void onClearFilters(ActionEvent e) {
        categoryCombo.getSelectionModel().select("All");
        priceSlider.setValue(priceSlider.getMax());
        searchField.clear();
    }

    @FXML private void navHome(ActionEvent e)     { SceneManager.getInstance().switchTo("dashboard", true); }
    @FXML private void navProducts(ActionEvent e) { /* here */ }
    @FXML private void navCart(ActionEvent e)     { SceneManager.getInstance().switchTo("cart", true); }
    @FXML private void navOrders(ActionEvent e)   { SceneManager.getInstance().switchTo("orders", true); }
    @FXML private void navProfile(ActionEvent e)  { SceneManager.getInstance().switchTo("profile", true); }
    @FXML private void onLogout(ActionEvent e) {
        DataStore.logout();
        SceneManager.getInstance().switchTo("welcome", true);
    }
}
