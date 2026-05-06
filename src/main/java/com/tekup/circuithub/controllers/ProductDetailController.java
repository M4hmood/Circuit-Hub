package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.Product;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.ImageLoader;
import com.tekup.circuithub.utils.SceneManager;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Map;

public class ProductDetailController {

    @FXML private ImageView mainImage;
    @FXML private HBox thumbsRow;
    @FXML private Label categoryChip;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label stockLabel;
    @FXML private Label descLabel;
    @FXML private GridPane specsGrid;
    @FXML private Label qtyLabel;
    @FXML private Label breadcrumb;
    @FXML private Button addToCartBtn;

    private int quantity = 1;
    private Product product;

    @FXML
    public void initialize() {
        product = DataStore.getSelectedProduct();
        if (product == null) {
            SceneManager.getInstance().switchTo("products", true);
            return;
        }

        breadcrumb.setText("// products / " + product.getCategory().toLowerCase() + " / " + product.getName());
        categoryChip.setText(product.getCategory().toUpperCase());
        nameLabel.setText(product.getName());
        priceLabel.setText(String.format("$%.2f", product.getPrice()));
        descLabel.setText(product.getDescription());

        int s = product.getStock();
        if (s <= 0) { stockLabel.setText("// out of stock"); stockLabel.getStyleClass().add("stock-out"); addToCartBtn.setDisable(true); }
        else if (s < 20) { stockLabel.setText("// low stock · " + s + " left"); stockLabel.getStyleClass().add("stock-low"); }
        else { stockLabel.setText("// in stock · " + s + " units"); stockLabel.getStyleClass().add("stock-in"); }

        ImageLoader.setForProductAsync(mainImage, product, 420, 320);

        // Thumbnails — repeat main image with tinted overlays
        for (int i = 0; i < 3; i++) {
            ImageView t = new ImageView();
            t.setFitWidth(100); t.setFitHeight(70);
            t.setPreserveRatio(true); t.setSmooth(true);
            ImageLoader.setForProductAsync(t, product, 100, 70);
            StackPane frame = new StackPane(t);
            frame.getStyleClass().add("panel");
            frame.setStyle("-fx-padding: 6; -fx-cursor: hand;");
            frame.setPrefSize(110, 80);
            frame.setOnMouseClicked(e ->
                ImageLoader.setForProductAsync(mainImage, product, 420, 320));
            thumbsRow.getChildren().add(frame);
        }

        // Specs table
        int row = 0;
        for (Map.Entry<String, String> en : product.getSpecs().entrySet()) {
            Label k = new Label(en.getKey()); k.getStyleClass().add("spec-key");
            Label v = new Label(en.getValue()); v.getStyleClass().add("spec-value");
            HBox keyBox = new HBox(k); keyBox.getStyleClass().add("spec-row"); keyBox.setPrefWidth(180);
            HBox valBox = new HBox(v); valBox.getStyleClass().add("spec-row");
            specsGrid.add(keyBox, 0, row);
            specsGrid.add(valBox, 1, row);
            row++;
        }

        // Pulsing add-to-cart
        DropShadow glow = new DropShadow(14, Color.web("#00FF9C"));
        glow.setSpread(0.5);
        addToCartBtn.setEffect(glow);
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.4), addToCartBtn);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.035); pulse.setToY(1.035);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
        pulse.play();
    }

    @FXML private void onQtyMinus(ActionEvent e) {
        if (quantity > 1) { quantity--; qtyLabel.setText(String.valueOf(quantity)); }
    }
    @FXML private void onQtyPlus(ActionEvent e) {
        if (quantity < product.getStock()) { quantity++; qtyLabel.setText(String.valueOf(quantity)); }
    }

    @FXML private void onAddToCart(ActionEvent e) {
        DataStore.addToCart(product, quantity);
        addToCartBtn.setText("✓ ADDED · GO TO CART");
        addToCartBtn.setOnAction(x -> SceneManager.getInstance().switchTo("cart", true));
    }

    @FXML private void navHome(ActionEvent e)     { SceneManager.getInstance().switchTo("dashboard", true); }
    @FXML private void navProducts(ActionEvent e) { SceneManager.getInstance().switchTo("products", true); }
    @FXML private void navCart(ActionEvent e)     { SceneManager.getInstance().switchTo("cart", true); }
    @FXML private void navOrders(ActionEvent e)   { SceneManager.getInstance().switchTo("orders", true); }
    @FXML private void navProfile(ActionEvent e)  { SceneManager.getInstance().switchTo("profile", true); }
    @FXML private void onLogout(ActionEvent e) {
        DataStore.logout();
        SceneManager.getInstance().switchTo("welcome", true);
    }
}
