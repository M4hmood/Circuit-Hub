package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.User;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.Hashing;
import com.tekup.circuithub.utils.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignInController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void onLogin(ActionEvent e) {
        clearInvalid();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String pw = passwordField.getText() == null ? "" : passwordField.getText();

        boolean bad = false;
        if (email.isEmpty() || !email.contains("@")) { markInvalid(emailField); bad = true; }
        if (pw.isEmpty()) { markInvalid(passwordField); bad = true; }
        if (bad) { errorLabel.setText("Invalid email or password"); return; }

        User u = DataStore.findUserByEmail(email);
        if (u == null || !u.getPasswordHash().equals(Hashing.sha256(pw))) {
            markInvalid(emailField); markInvalid(passwordField);
            errorLabel.setText("Email or password incorrect");
            return;
        }
        DataStore.setCurrentUser(u);
        SceneManager.getInstance().switchTo(u.isAdmin() ? "admin-dashboard" : "dashboard", true);
    }

    @FXML
    private void onCreateAccount(ActionEvent e) {
        SceneManager.getInstance().switchTo("signup", true);
    }

    private void markInvalid(javafx.scene.control.Control c) {
        if (!c.getStyleClass().contains("invalid")) c.getStyleClass().add("invalid");
    }
    private void clearInvalid() {
        emailField.getStyleClass().remove("invalid");
        passwordField.getStyleClass().remove("invalid");
        errorLabel.setText(" ");
    }
}
