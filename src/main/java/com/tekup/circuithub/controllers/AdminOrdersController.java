package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.CartItem;
import com.tekup.circuithub.models.Order;
import com.tekup.circuithub.models.User;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AdminOrdersController {

    private static final String[] STATUSES = {"Pending", "Shipped", "Delivered", "Cancelled"};

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> colOrderId;
    @FXML private TableColumn<Order, String> colUserId;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colItems;
    @FXML private TableColumn<Order, String> colTotal;
    @FXML private TableColumn<Order, Void> colStatus;
    @FXML private TableColumn<Order, Void> colDetails;
    @FXML private ComboBox<String> filterCombo;
    @FXML private VBox detailsPanel;
    @FXML private VBox detailsBox;
    @FXML private Label detailsTitle;

    private final ObservableList<Order> all = FXCollections.observableArrayList();
    private FilteredList<Order> filtered;

    @FXML
    public void initialize() {
        User u = DataStore.getCurrentUser();
        if (u == null || !u.isAdmin()) {
            SceneManager.getInstance().switchTo("welcome", true);
            return;
        }

        colOrderId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        colUserId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUserId()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate()));
        colItems.setCellValueFactory(c -> new SimpleStringProperty(
                String.valueOf(c.getValue().getItems() == null ? 0 : c.getValue().getItems().size())));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%.2f", c.getValue().getTotal())));

        colStatus.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(STATUSES));
            {
                combo.getStyleClass().addAll("combo", "combo-box");
                combo.setOnAction(e -> {
                    Order o = getTableView().getItems().get(getIndex());
                    String newStatus = combo.getValue();
                    if (newStatus != null && !newStatus.equals(o.getStatus())) {
                        if (DataStore.updateOrderStatus(o.getId(), newStatus)) {
                            o.setStatus(newStatus);
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Order o = getTableView().getItems().get(getIndex());
                combo.setValue(o.getStatus());
                setGraphic(combo);
            }
        });

        colDetails.setCellFactory(col -> new TableCell<>() {
            private final Button view = new Button("View Items");
            private final HBox box = new HBox(view);
            {
                view.getStyleClass().add("btn-secondary");
                box.getStyleClass().add("action-row");
                view.setOnAction(e -> showItems(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        filterCombo.setItems(FXCollections.observableArrayList("All", "Pending", "Shipped", "Delivered", "Cancelled"));
        filterCombo.setValue("All");
        filterCombo.setOnAction(e -> applyFilter());

        refresh();
    }

    private void refresh() {
        all.setAll(DataStore.loadAllOrders());
        filtered = new FilteredList<>(all, o -> true);
        ordersTable.setItems(filtered);
        applyFilter();
    }

    private void applyFilter() {
        if (filtered == null) return;
        String f = filterCombo.getValue();
        if (f == null || "All".equals(f)) { filtered.setPredicate(o -> true); return; }
        filtered.setPredicate(o -> f.equalsIgnoreCase(o.getStatus()));
    }

    private void showItems(Order o) {
        detailsPanel.setVisible(true);
        detailsPanel.setManaged(true);
        detailsTitle.setText("Items for " + o.getId());
        detailsBox.getChildren().clear();
        if (o.getItems() == null || o.getItems().isEmpty()) {
            Label empty = new Label("No items linked.");
            empty.getStyleClass().add("text-muted");
            detailsBox.getChildren().add(empty);
            return;
        }
        for (CartItem ci : o.getItems()) {
            HBox row = new HBox(14);
            row.getStyleClass().add("order-row");
            Label name = new Label(ci.getProduct().getName());
            name.getStyleClass().add("text-primary");
            Label qty = new Label("x " + ci.getQuantity());
            qty.getStyleClass().add("text-muted");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label line = new Label(String.format("$%.2f", ci.getLineTotal()));
            line.getStyleClass().add("text-accent");
            row.getChildren().addAll(name, qty, spacer, line);
            detailsBox.getChildren().add(row);
        }
    }

    @FXML private void navAdminDashboard(ActionEvent e) { SceneManager.getInstance().switchTo("admin-dashboard", true); }
    @FXML private void navAdminProducts(ActionEvent e)  { SceneManager.getInstance().switchTo("admin-products", true); }
    @FXML private void navAdminOrders(ActionEvent e)    { /* current */ }
    @FXML private void navAdminUsers(ActionEvent e)     { SceneManager.getInstance().switchTo("admin-users", true); }
    @FXML private void onLogout(ActionEvent e) {
        DataStore.logout();
        SceneManager.getInstance().switchTo("welcome", true);
    }
}
