package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.CartItem;
import com.tekup.circuithub.models.Order;
import com.tekup.circuithub.models.User;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.SceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileController {

    @FXML private HBox identityCard;
    @FXML private GridPane statsGrid;
    @FXML private Label initialsLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label joinLabel;
    @FXML private Label idLabel;
    @FXML private Label statOrders;
    @FXML private Label statOrdersSub;
    @FXML private Label statSpent;
    @FXML private Label statSpentSub;
    @FXML private Label statFavorite;
    @FXML private Label statFavoriteSub;

    @FXML
    public void initialize() {
        User u = DataStore.getCurrentUser();
        if (u == null) {
            SceneManager.getInstance().switchTo("welcome", true);
            return;
        }

        nameLabel.setText(u.getFullName());
        emailLabel.setText(u.getEmail());
        joinLabel.setText("Joined " + u.getJoinDate());
        idLabel.setText("ID: " + u.getId().substring(0, 8));
        initialsLabel.setText(initials(u.getFullName()));

        List<Order> orders = DataStore.loadOrders().stream()
                .filter(o -> u.getId().equals(o.getUserId()))
                .toList();

        int total = orders.size();
        int pending = 0, shipped = 0, delivered = 0;
        double spent = 0;
        Map<String, Integer> catCount = new HashMap<>();

        for (Order o : orders) {
            spent += o.getTotal();
            switch (o.getStatus() == null ? "" : o.getStatus()) {
                case "Shipped"   -> shipped++;
                case "Delivered" -> delivered++;
                default          -> pending++;
            }
            for (CartItem ci : o.getItems()) {
                catCount.merge(ci.getProduct().getCategory(), ci.getQuantity(), Integer::sum);
            }
        }

        statOrders.setText(String.valueOf(total));
        statOrdersSub.setText(pending + " pending · " + shipped + " shipped · " + delivered + " delivered");

        statSpent.setText(String.format("$%.2f", spent));
        double avg = total == 0 ? 0 : spent / total;
        statSpentSub.setText(String.format("avg $%.2f / order", avg));

        if (catCount.isEmpty()) {
            statFavorite.setText("—");
            statFavoriteSub.setText("no purchases yet");
        } else {
            String fav = catCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).get().getKey();
            statFavorite.setText(fav);
            statFavoriteSub.setText(catCount.get(fav) + " units purchased");
        }

        animateEntrance();
    }

    private void animateEntrance() {
        identityCard.setOpacity(0);
        identityCard.setTranslateY(18);
        FadeTransition ft = new FadeTransition(Duration.millis(300), identityCard);
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), identityCard);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(ft, tt).play();

        PauseTransition delay = new PauseTransition(Duration.millis(140));
        delay.setOnFinished(e -> SceneManager.staggerIn(statsGrid.getChildren()));
        delay.play();
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "--";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0)));
            if (sb.length() >= 2) break;
        }
        return sb.toString();
    }

    @FXML private void navHome(ActionEvent e)     { SceneManager.getInstance().switchTo("dashboard", true); }
    @FXML private void navProducts(ActionEvent e) { SceneManager.getInstance().switchTo("products", true); }
    @FXML private void navCart(ActionEvent e)     { SceneManager.getInstance().switchTo("cart", true); }
    @FXML private void navOrders(ActionEvent e)   { SceneManager.getInstance().switchTo("orders", true); }
    @FXML private void navProfile(ActionEvent e)  { /* here */ }
    @FXML private void onLogout(ActionEvent e) {
        DataStore.logout();
        SceneManager.getInstance().switchTo("welcome", true);
    }
}
