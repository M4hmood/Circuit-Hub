package com.tekup.circuithub.controllers;

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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class AdminUsersController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colJoin;
    @FXML private TableColumn<User, Void> colRole;
    @FXML private TableColumn<User, Void> colActions;
    @FXML private TextField searchField;

    private final ObservableList<User> all = FXCollections.observableArrayList();
    private FilteredList<User> filtered;

    @FXML
    public void initialize() {
        User me = DataStore.getCurrentUser();
        if (me == null || !me.isAdmin()) {
            SceneManager.getInstance().switchTo("welcome", true);
            return;
        }

        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colJoin.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJoinDate()));

        colRole.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User u = getTableView().getItems().get(getIndex());
                badge.setText(u.isAdmin() ? "ADMIN" : "USER");
                badge.getStyleClass().setAll(u.isAdmin() ? "badge-admin" : "badge-user");
                setGraphic(badge);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button toggleBtn = new Button();
            private final Button delBtn = new Button("Delete");
            private final HBox box = new HBox(6, toggleBtn, delBtn);
            {
                toggleBtn.getStyleClass().add("btn-secondary");
                delBtn.getStyleClass().add("btn-danger");
                box.getStyleClass().add("action-row");
                toggleBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    String newRole = u.isAdmin() ? "USER" : "ADMIN";
                    if (DataStore.updateUserRole(u.getId(), newRole)) refresh();
                });
                delBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete user " + u.getFullName() + "? Their orders will also be removed.",
                            ButtonType.YES, ButtonType.CANCEL);
                    a.setHeaderText("Confirm delete");
                    a.showAndWait().ifPresent(bt -> {
                        if (bt == ButtonType.YES && DataStore.deleteUser(u.getId())) refresh();
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User u = getTableView().getItems().get(getIndex());
                User self = DataStore.getCurrentUser();
                boolean isSelf = self != null && self.getId().equals(u.getId());
                toggleBtn.setText(u.isAdmin() ? "Demote" : "Promote");
                box.getChildren().clear();
                if (!isSelf) {
                    box.getChildren().addAll(toggleBtn, delBtn);
                } else {
                    Label tag = new Label("(you)");
                    tag.getStyleClass().add("text-muted");
                    box.getChildren().add(tag);
                }
                setGraphic(box);
            }
        });

        refresh();

        searchField.textProperty().addListener((obs, o, n) -> applyFilter(n));
    }

    private void refresh() {
        all.setAll(DataStore.loadUsers());
        filtered = new FilteredList<>(all, u -> true);
        usersTable.setItems(filtered);
        applyFilter(searchField == null ? "" : searchField.getText());
    }

    private void applyFilter(String q) {
        if (filtered == null) return;
        if (q == null || q.isBlank()) { filtered.setPredicate(u -> true); return; }
        String needle = q.toLowerCase();
        filtered.setPredicate(u ->
                (u.getFullName() != null && u.getFullName().toLowerCase().contains(needle))
             || (u.getEmail()    != null && u.getEmail().toLowerCase().contains(needle))
             || (u.getId()       != null && u.getId().toLowerCase().contains(needle)));
    }

    @FXML private void navAdminDashboard(ActionEvent e) { SceneManager.getInstance().switchTo("admin-dashboard", true); }
    @FXML private void navAdminProducts(ActionEvent e)  { SceneManager.getInstance().switchTo("admin-products", true); }
    @FXML private void navAdminOrders(ActionEvent e)    { SceneManager.getInstance().switchTo("admin-orders", true); }
    @FXML private void navAdminUsers(ActionEvent e)     { /* current */ }
    @FXML private void onLogout(ActionEvent e) {
        DataStore.logout();
        SceneManager.getInstance().switchTo("welcome", true);
    }
}
