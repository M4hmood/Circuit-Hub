package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.Order;
import com.tekup.circuithub.models.Product;
import com.tekup.circuithub.models.User;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminDashboardController {

    @FXML private Label revenueValue;
    @FXML private Label ordersValue;
    @FXML private Label usersValue;
    @FXML private Label productsValue;
    @FXML private Label lowStockValue;
    @FXML private VBox recentOrdersBox;
    @FXML private VBox lowStockBox;

    @FXML
    public void initialize() {
        User u = DataStore.getCurrentUser();
        if (u == null || !u.isAdmin()) {
            SceneManager.getInstance().switchTo("welcome", true);
            return;
        }

        DataStore.AdminStats s = DataStore.loadAdminStats();
        revenueValue.setText(String.format("$%.2f", s.totalRevenue()));
        ordersValue.setText(String.valueOf(s.orderCount()));
        usersValue.setText(String.valueOf(s.userCount()));
        productsValue.setText(String.valueOf(s.productCount()));
        lowStockValue.setText(String.valueOf(s.lowStockCount()));

        renderRecentOrders();
        renderLowStock();
    }

    private void renderRecentOrders() {
        recentOrdersBox.getChildren().clear();
        List<Order> orders = DataStore.loadAllOrders();
        if (orders.isEmpty()) {
            Label empty = new Label("No orders yet");
            empty.getStyleClass().add("text-muted");
            recentOrdersBox.getChildren().add(empty);
            return;
        }
        int max = Math.min(5, orders.size());
        for (int i = 0; i < max; i++) {
            Order o = orders.get(i);
            HBox row = new HBox(14);
            row.getStyleClass().add("order-row");
            Label id = new Label(o.getId());
            id.getStyleClass().add("text-accent");
            Label user = new Label(o.getUserId());
            user.getStyleClass().add("text-muted");
            Label date = new Label(o.getDate());
            date.getStyleClass().add("text-secondary");
            Label total = new Label(String.format("$%.2f", o.getTotal()));
            total.getStyleClass().add("text-primary");
            Label status = new Label(o.getStatus());
            status.getStyleClass().add(badgeClassFor(o.getStatus()));
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(id, user, date, total, spacer, status);
            recentOrdersBox.getChildren().add(row);
        }
    }

    private void renderLowStock() {
        lowStockBox.getChildren().clear();
        List<Product> products = DataStore.loadProducts();
        boolean any = false;
        for (Product p : products) {
            if (p.getStock() >= 20) continue;
            any = true;
            HBox row = new HBox(14);
            row.getStyleClass().add("order-row");
            Label id = new Label(p.getId());
            id.getStyleClass().add("text-accent");
            Label name = new Label(p.getName());
            name.getStyleClass().add("text-primary");
            Label cat = new Label(p.getCategory());
            cat.getStyleClass().add("text-muted");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label stock = new Label("stock: " + p.getStock());
            stock.getStyleClass().add(p.getStock() == 0 ? "badge-cancelled" : "badge-pending");
            row.getChildren().addAll(id, name, cat, spacer, stock);
            lowStockBox.getChildren().add(row);
        }
        if (!any) {
            Label empty = new Label("All products well stocked.");
            empty.getStyleClass().add("text-muted");
            lowStockBox.getChildren().add(empty);
        }
    }

    private String badgeClassFor(String status) {
        if (status == null) return "badge-pending";
        return switch (status.toLowerCase()) {
            case "shipped" -> "badge-shipped";
            case "delivered" -> "badge-delivered";
            case "cancelled" -> "badge-cancelled";
            default -> "badge-pending";
        };
    }

    @FXML private void navAdminDashboard(ActionEvent e) { /* current */ }
    @FXML private void navAdminProducts(ActionEvent e) { SceneManager.getInstance().switchTo("admin-products", true); }
    @FXML private void navAdminOrders(ActionEvent e)   { SceneManager.getInstance().switchTo("admin-orders", true); }
    @FXML private void navAdminUsers(ActionEvent e)    { SceneManager.getInstance().switchTo("admin-users", true); }
    @FXML private void onLogout(ActionEvent e) {
        DataStore.logout();
        SceneManager.getInstance().switchTo("welcome", true);
    }
}
