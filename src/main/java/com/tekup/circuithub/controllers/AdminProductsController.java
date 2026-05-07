package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.Product;
import com.tekup.circuithub.models.User;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.Money;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.Set;
import java.util.TreeSet;

public class AdminProductsController {

    private static final String STOCK_ALL = "All";
    private static final String STOCK_IN  = "In stock";
    private static final String STOCK_LOW = "Low (< 20)";
    private static final String STOCK_OUT = "Out of stock";

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, String> colPrice;
    @FXML private TableColumn<Product, Number> colStock;
    @FXML private TableColumn<Product, Void> colActions;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> stockCombo;
    @FXML private Label kpiTotal;
    @FXML private Label kpiInStock;
    @FXML private Label kpiLow;
    @FXML private Label kpiOut;

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
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(Money.format(c.getValue().getPrice())));
        colStock.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getStock()));

        colStock.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                getStyleClass().removeAll("stock-cell-good", "stock-cell-low", "stock-cell-bad");
                if (empty || value == null) { setText(null); return; }
                int s = value.intValue();
                setText(String.valueOf(s));
                if (s == 0) getStyleClass().add("stock-cell-bad");
                else if (s < 20) getStyleClass().add("stock-cell-low");
                else getStyleClass().add("stock-cell-good");
            }
        });

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

        stockCombo.setItems(FXCollections.observableArrayList(STOCK_ALL, STOCK_IN, STOCK_LOW, STOCK_OUT));
        stockCombo.setValue(STOCK_ALL);
        stockCombo.valueProperty().addListener((o, a, b) -> applyFilter());

        refresh();

        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        categoryCombo.valueProperty().addListener((o, a, b) -> applyFilter());
    }

    private void refresh() {
        all.setAll(DataStore.loadProducts());
        filtered = new FilteredList<>(all, p -> true);
        productsTable.setItems(filtered);

        Set<String> cats = new TreeSet<>();
        cats.add("All");
        for (Product p : all) cats.add(p.getCategory());
        String prev = categoryCombo.getValue();
        categoryCombo.setItems(FXCollections.observableArrayList(cats));
        categoryCombo.setValue(prev != null && cats.contains(prev) ? prev : "All");

        updateKpis();
        applyFilter();
    }

    private void updateKpis() {
        int total = all.size();
        int in = 0, low = 0, out = 0;
        for (Product p : all) {
            int s = p.getStock();
            if (s == 0) out++;
            else if (s < 20) low++;
            else in++;
        }
        if (kpiTotal != null) kpiTotal.setText(String.valueOf(total));
        if (kpiInStock != null) kpiInStock.setText(String.valueOf(in));
        if (kpiLow != null) kpiLow.setText(String.valueOf(low));
        if (kpiOut != null) kpiOut.setText(String.valueOf(out));
    }

    private void applyFilter() {
        if (filtered == null) return;
        String q = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String cat = categoryCombo == null ? "All" : categoryCombo.getValue();
        String stockFilter = stockCombo == null ? STOCK_ALL : stockCombo.getValue();
        filtered.setPredicate(p -> {
            if (!q.isEmpty()
                    && !p.getId().toLowerCase().contains(q)
                    && !p.getName().toLowerCase().contains(q)
                    && !p.getCategory().toLowerCase().contains(q)) return false;
            if (cat != null && !"All".equals(cat) && !p.getCategory().equals(cat)) return false;
            int s = p.getStock();
            return switch (stockFilter == null ? STOCK_ALL : stockFilter) {
                case STOCK_IN -> s >= 20;
                case STOCK_LOW -> s > 0 && s < 20;
                case STOCK_OUT -> s == 0;
                default -> true;
            };
        });
    }

    @FXML private void onNew(ActionEvent e) {
        DataStore.setSelectedProduct(null);
        SceneManager.getInstance().switchTo("admin-product-edit", true);
    }

    @FXML private void onClearFilters(ActionEvent e) {
        if (searchField != null) searchField.clear();
        if (categoryCombo != null) categoryCombo.setValue("All");
        if (stockCombo != null) stockCombo.setValue(STOCK_ALL);
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
