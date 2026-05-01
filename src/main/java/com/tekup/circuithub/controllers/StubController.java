package com.tekup.circuithub.controllers;

import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class StubController {
    @FXML private Label info;

    @FXML
    public void initialize() {
        if (info != null) {
            int n = DataStore.getCart().size();
            info.setText("$ cart_items: " + n + "  ·  subtotal: $" + String.format("%.2f", DataStore.cartSubtotal()));
        }
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
