package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.CartItem;
import com.tekup.circuithub.models.Order;
import com.tekup.circuithub.models.User;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.Money;
import com.tekup.circuithub.utils.SceneManager;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OrdersController {

    @FXML private VBox ordersBox;
    @FXML private Label countLabel;
    @FXML private ComboBox<String> statusCombo;

    private List<Order> userOrders;

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList("All", "Pending", "Shipped", "Delivered"));
        statusCombo.getSelectionModel().select("All");
        statusCombo.valueProperty().addListener((o, a, b) -> render());

        User u = DataStore.getCurrentUser();
        String uid = u == null ? "guest" : u.getId();
        userOrders = DataStore.loadOrders().stream()
                .filter(o -> uid.equals(o.getUserId()))
                .sorted(Comparator.comparing(Order::getDate).reversed())
                .collect(Collectors.toList());

        render();
    }

    private void render() {
        ordersBox.getChildren().clear();
        String filter = statusCombo.getValue();
        List<Order> list = userOrders.stream()
                .filter(o -> "All".equals(filter) || filter == null || filter.equals(o.getStatus()))
                .collect(Collectors.toList());

        countLabel.setText("// " + list.size() + " orders");

        if (list.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(javafx.geometry.Pos.CENTER);
            empty.setPrefHeight(320);
            Label t = new Label("// no orders yet");
            t.getStyleClass().add("empty-title");
            Button browse = new Button("▤ BROWSE PRODUCTS");
            browse.getStyleClass().add("btn-secondary");
            browse.setOnAction(e -> SceneManager.getInstance().switchTo("products", true));
            empty.getChildren().addAll(t, browse);
            ordersBox.getChildren().add(empty);
            return;
        }

        for (Order o : list) ordersBox.getChildren().add(buildRow(o));
        SceneManager.staggerIn(ordersBox.getChildren());
    }

    private VBox buildRow(Order o) {
        VBox container = new VBox(12);
        container.getStyleClass().add("order-row");

        HBox header = new HBox(16);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox idBox = new VBox(2);
        Label id = new Label(o.getId()); id.getStyleClass().add("order-id");
        Label date = new Label("// " + o.getDate() + "  ·  " + o.getItems().size() + " lines");
        date.getStyleClass().add("order-date");
        idBox.getChildren().addAll(id, date);

        Label badge = new Label(o.getStatus().toUpperCase());
        badge.getStyleClass().addAll("badge", badgeClassFor(o.getStatus()));

        Label total = new Label(Money.format(o.getTotal()));
        total.getStyleClass().add("order-total");

        Button toggle = new Button("▾ VIEW DETAILS");
        toggle.getStyleClass().add("btn-ghost");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(idBox, badge, spacer, total, toggle);

        VBox details = buildDetails(o);
        details.setVisible(false);
        details.setManaged(false);

        toggle.setOnAction(e -> {
            boolean open = !details.isVisible();
            if (open) {
                details.setOpacity(0);
                details.setVisible(true);
                details.setManaged(true);
                FadeTransition ft = new FadeTransition(Duration.millis(220), details);
                ft.setFromValue(0); ft.setToValue(1); ft.play();
                toggle.setText("▴ HIDE DETAILS");
            } else {
                FadeTransition ft = new FadeTransition(Duration.millis(180), details);
                ft.setFromValue(1); ft.setToValue(0);
                ft.setOnFinished(x -> { details.setVisible(false); details.setManaged(false); });
                ft.play();
                toggle.setText("▾ VIEW DETAILS");
            }
        });

        container.getChildren().addAll(header, details);
        return container;
    }

    private VBox buildDetails(Order o) {
        VBox box = new VBox(8);
        box.getStyleClass().add("order-details");

        Label hdr = new Label("// line items");
        hdr.getStyleClass().add("page-sub");
        box.getChildren().add(hdr);

        for (CartItem ci : o.getItems()) {
            HBox row = new HBox(14);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label qty = new Label("x" + ci.getQuantity());
            qty.getStyleClass().add("mono");
            qty.setStyle("-fx-text-fill: #00D9FF; -fx-min-width: 40;");
            Label name = new Label(ci.getProduct().getName());
            name.getStyleClass().add("spec-value");
            Label cat = new Label(ci.getProduct().getCategory().toUpperCase());
            cat.getStyleClass().add("chip-category");
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Label line = new Label(Money.format(ci.getLineTotal()));
            line.getStyleClass().add("price");
            row.getChildren().addAll(qty, cat, name, sp, line);
            box.getChildren().add(row);
        }

        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #1F2937;");
        box.getChildren().add(divider);

        HBox totalRow = new HBox();
        totalRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Label totalLbl = new Label("TOTAL  ");
        totalLbl.getStyleClass().add("total-key");
        Label total = new Label(Money.format(o.getTotal()));
        total.getStyleClass().add("order-total");
        totalRow.getChildren().addAll(totalLbl, total);
        box.getChildren().add(totalRow);

        return box;
    }

    private String badgeClassFor(String status) {
        return switch (status == null ? "" : status) {
            case "Shipped"   -> "badge-shipped";
            case "Delivered" -> "badge-delivered";
            default          -> "badge-pending";
        };
    }

    @FXML private void navHome(ActionEvent e)     { SceneManager.getInstance().switchTo("dashboard", true); }
    @FXML private void navProducts(ActionEvent e) { SceneManager.getInstance().switchTo("products", true); }
    @FXML private void navCart(ActionEvent e)     { SceneManager.getInstance().switchTo("cart", true); }
    @FXML private void navOrders(ActionEvent e)   { /* here */ }
    @FXML private void navProfile(ActionEvent e)  { SceneManager.getInstance().switchTo("profile", true); }
    @FXML private void onLogout(ActionEvent e) {
        DataStore.logout();
        SceneManager.getInstance().switchTo("welcome", true);
    }
}
