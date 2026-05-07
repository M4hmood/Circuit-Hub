package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.CartItem;
import com.tekup.circuithub.models.Order;
import com.tekup.circuithub.models.User;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.ImageLoader;
import com.tekup.circuithub.utils.Money;
import com.tekup.circuithub.utils.SceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CartController {

    private static final double TAX_RATE = 0.10;

    @FXML private VBox itemsBox;
    @FXML private Label itemCountLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private Button checkoutBtn;
    @FXML private HBox totalsBar;
    @FXML private StackPane successOverlay;
    @FXML private Label checkLabel;
    @FXML private Label successSub;

    @FXML
    public void initialize() {
        render();
    }

    private void render() {
        itemsBox.getChildren().clear();
        List<CartItem> cart = DataStore.getCart();

        if (cart.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(javafx.geometry.Pos.CENTER);
            empty.setPrefHeight(320);
            Label big = new Label("// your cart is empty");
            big.getStyleClass().add("empty-title");
            Button browse = new Button("▤ BROWSE PRODUCTS");
            browse.getStyleClass().add("btn-secondary");
            browse.setOnAction(e -> SceneManager.getInstance().switchTo("products", true));
            empty.getChildren().addAll(big, browse);
            itemsBox.getChildren().add(empty);
            checkoutBtn.setDisable(true);
        } else {
            for (CartItem ci : new ArrayList<>(cart)) itemsBox.getChildren().add(buildRow(ci));
            checkoutBtn.setDisable(false);
            SceneManager.staggerIn(itemsBox.getChildren());
        }

        itemCountLabel.setText("// " + cart.stream().mapToInt(CartItem::getQuantity).sum() + " items");
        double subtotal = DataStore.cartSubtotal();
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;
        subtotalLabel.setText(Money.format(subtotal));
        taxLabel.setText(Money.format(tax));
        totalLabel.setText(Money.format(total));
    }

    private HBox buildRow(CartItem ci) {
        HBox row = new HBox(16);
        row.getStyleClass().add("cart-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        ImageView iv = new ImageView();
        iv.setFitWidth(88); iv.setFitHeight(64);
        iv.setPreserveRatio(true); iv.setSmooth(true);
        ImageLoader.setForProductAsync(iv, ci.getProduct(), 88, 64);
        StackPane thumb = new StackPane(iv);
        thumb.getStyleClass().add("product-thumb");
        thumb.setPrefSize(96, 72);
        thumb.setStyle(thumb.getStyle() + "; -fx-alignment: center; -fx-padding: 4;");

        Label name = new Label(ci.getProduct().getName());
        name.getStyleClass().add("product-name");
        Label cat = new Label(ci.getProduct().getCategory().toUpperCase());
        cat.getStyleClass().add("chip-category");
        VBox info = new VBox(4, cat, name);
        info.setPrefWidth(300);

        Label unit = new Label(Money.formatEach(ci.getProduct().getPrice()));
        unit.getStyleClass().add("page-sub");

        Button minus = new Button("−"); minus.getStyleClass().add("qty-btn");
        Label qty = new Label(String.valueOf(ci.getQuantity())); qty.getStyleClass().add("qty-value");
        Button plus = new Button("+"); plus.getStyleClass().add("qty-btn");
        HBox qtyBox = new HBox(8, minus, qty, plus); qtyBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label line = new Label(Money.format(ci.getLineTotal()));
        line.getStyleClass().add("price"); line.setStyle("-fx-font-size: 18px;");
        line.setPrefWidth(100);
        line.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button remove = new Button("✕ REMOVE");
        remove.getStyleClass().add("btn-remove");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(thumb, info, unit, spacer, qtyBox, line, remove);

        minus.setOnAction(e -> { DataStore.updateCartQty(ci.getProduct().getId(), ci.getQuantity() - 1); render(); });
        plus.setOnAction(e -> {
            if (ci.getQuantity() < ci.getProduct().getStock()) {
                DataStore.updateCartQty(ci.getProduct().getId(), ci.getQuantity() + 1);
                render();
            }
        });
        remove.setOnAction(e -> { DataStore.removeFromCart(ci.getProduct().getId()); render(); });

        return row;
    }

    @FXML
    private void onCheckout(ActionEvent e) {
        double total = DataStore.cartSubtotal() * (1 + TAX_RATE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Order");
        alert.setHeaderText("Place this order?");
        alert.setContentText(String.format("Total: %s (incl. 10%% tax)%n%nItems: %d",
                Money.format(total), DataStore.getCart().stream().mapToInt(CartItem::getQuantity).sum()));
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/tekup/circuithub/styles/app.css").toExternalForm());

        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) placeOrder(total);
    }

    private void placeOrder(double total) {
        User u = DataStore.getCurrentUser();
        Order order = new Order();
        order.setId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUserId(u == null ? "guest" : u.getId());
        order.setItems(new ArrayList<>(DataStore.getCart()));
        order.setTotal(total);
        order.setDate(LocalDate.now().toString());
        order.setStatus("Pending");
        DataStore.addOrder(order);
        DataStore.clearCart();

        successSub.setText("// " + order.getId());
        showSuccess();
    }

    private void showSuccess() {
        successOverlay.setVisible(true);
        successOverlay.setManaged(true);
        successOverlay.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(260), successOverlay);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        ScaleTransition pop = new ScaleTransition(Duration.millis(420), checkLabel);
        pop.setFromX(0.4); pop.setFromY(0.4);
        pop.setToX(1.0); pop.setToY(1.0);

        ParallelTransition enter = new ParallelTransition(fadeIn, pop);
        enter.play();

        PauseTransition hold = new PauseTransition(Duration.millis(1600));
        hold.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(280), successOverlay);
            fadeOut.setFromValue(1); fadeOut.setToValue(0);
            fadeOut.setOnFinished(x -> {
                successOverlay.setVisible(false);
                successOverlay.setManaged(false);
                SceneManager.getInstance().switchTo("orders", true);
            });
            fadeOut.play();
        });
        enter.setOnFinished(e -> hold.play());
    }

    @FXML private void navHome(ActionEvent e)     { SceneManager.getInstance().switchTo("dashboard", true); }
    @FXML private void navProducts(ActionEvent e) { SceneManager.getInstance().switchTo("products", true); }
    @FXML private void navCart(ActionEvent e)     { /* here */ }
    @FXML private void navOrders(ActionEvent e)   { SceneManager.getInstance().switchTo("orders", true); }
    @FXML private void navProfile(ActionEvent e)  { SceneManager.getInstance().switchTo("profile", true); }
    @FXML private void onLogout(ActionEvent e) {
        DataStore.logout();
        SceneManager.getInstance().switchTo("welcome", true);
    }
}
