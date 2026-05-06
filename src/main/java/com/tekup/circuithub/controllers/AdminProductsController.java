package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.Product;
import com.tekup.circuithub.models.User;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class AdminProductsController {

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, String> colPrice;
    @FXML private TableColumn<Product, String> colStock;
    @FXML private TableColumn<Product, Void> colActions;
    @FXML private TextField searchField;

    private final ObservableList<Product> all = FXCollections.observableArrayList();
    private FilteredList<Product> filtered;

    @FXML
    public void initialize() {
        User u = DataStore.getCurrentUser();
        if (u == null || !u.isAdmin()) {
            SceneManager.getInstance().switchTo("welcome", true);
            return;
        }

        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%.2f", c.getValue().getPrice())));
        colStock.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getStock())));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button delBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, delBtn);
            {
                editBtn.getStyleClass().add("btn-secondary");
                delBtn.getStyleClass().add("btn-danger");
                box.getStyleClass().add("action-row");
                editBtn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    DataStore.setSelectedProduct(p);
                    SceneManager.getInstance().switchTo("admin-product-edit", true);
                });
                delBtn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete product " + p.getName() + "? This will also remove it from past order line items.",
                            ButtonType.YES, ButtonType.CANCEL);
                    a.setHeaderText("Confirm delete");
                    a.showAndWait().ifPresent(bt -> {
                        if (bt == ButtonType.YES) {
                            if (DataStore.deleteProduct(p.getId())) refresh();
                        }
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        refresh();

        searchField.textProperty().addListener((obs, o, n) -> applyFilter(n));
    }

    private void refresh() {
        all.setAll(DataStore.loadProducts());
        filtered = new FilteredList<>(all, p -> true);
        productsTable.setItems(filtered);
        applyFilter(searchField == null ? "" : searchField.getText());
    }

    private void applyFilter(String q) {
        if (filtered == null) return;
        if (q == null || q.isBlank()) { filtered.setPredicate(p -> true); return; }
        String needle = q.toLowerCase();
        filtered.setPredicate(p ->
                p.getId().toLowerCase().contains(needle)
             || p.getName().toLowerCase().contains(needle)
             || p.getCategory().toLowerCase().contains(needle));
    }

    @FXML private void onNew(ActionEvent e) {
        DataStore.setSelectedProduct(null);
        SceneManager.getInstance().switchTo("admin-product-edit", true);
    }

    @FXML private void navAdminDashboard(ActionEvent e) { SceneManager.getInstance().switchTo("admin-dashboard", true); }
    @FXML private void navAdminProducts(ActionEvent e)  { /* current */ }
    @FXML private void navAdminOrders(ActionEvent e)    { SceneManager.getInstance().switchTo("admin-orders", true); }
    @FXML private void navAdminUsers(ActionEvent e)     { SceneManager.getInstance().switchTo("admin-users", true); }
    @FXML private void onLogout(ActionEvent e) {
        DataStore.logout();
        SceneManager.getInstance().switchTo("welcome", true);
    }
}
