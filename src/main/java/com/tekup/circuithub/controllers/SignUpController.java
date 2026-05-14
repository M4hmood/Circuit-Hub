package com.tekup.circuithub.controllers;

import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.Hashing;
import com.tekup.circuithub.utils.Mailer;
import com.tekup.circuithub.utils.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class SignUpController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    @FXML private Label errorLabel;
    @FXML private Button submitButton;

    @FXML
    public void initialize() {
        passwordField.textProperty().addListener((o, a, pw) -> updateStrength(pw));
    }

    private void updateStrength(String pw) {
        int score = 0;
        if (pw == null) pw = "";
        if (pw.length() >= 6) score++;
        if (pw.length() >= 10) score++;
        if (pw.matches(".*[A-Z].*")) score++;
        if (pw.matches(".*[0-9].*")) score++;
        if (pw.matches(".*[^a-zA-Z0-9].*")) score++;
        double pct = Math.min(1.0, score / 5.0);
        strengthBar.setProgress(pct);
        String color; String label;
        if (score <= 1) { color = "#EF4444"; label = "Weak"; }
        else if (score <= 3) { color = "#F59E0B"; label = "Medium"; }
        else { color = "#10B981"; label = "Strong"; }
        strengthBar.setStyle("-fx-accent: " + color + ";");
        strengthLabel.setText("Strength: " + label);
        strengthLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px;");
    }

    @FXML
    private void onSignUp(ActionEvent e) {
        clearInvalid();
        String name = safe(nameField.getText());
        String email = safe(emailField.getText());
        String pw = safe(passwordField.getText());
        String confirm = safe(confirmField.getText());

        boolean bad = false;
        if (name.isEmpty()) { markInvalid(nameField); bad = true; }
        if (email.isEmpty() || !email.contains("@")) { markInvalid(emailField); bad = true; }
        if (pw.length() < 6) { markInvalid(passwordField); bad = true; }
        if (!pw.equals(confirm)) { markInvalid(confirmField); bad = true; }
        if (bad) { errorLabel.setText("Check all fields (password ≥ 6 characters and must match)"); return; }

        if (DataStore.findUserByEmail(email) != null) {
            markInvalid(emailField);
            errorLabel.setText("Email already registered");
            return;
        }

        String code = DataStore.generateVerificationCode();
        DataStore.putPending(name, email, Hashing.sha256(pw), code);

        if (submitButton != null) submitButton.setDisable(true);
        errorLabel.setText("Sending verification code…");
        errorLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");

        Mailer.sendVerificationCode(email, code).whenComplete((ok, err) -> Platform.runLater(() -> {
            if (submitButton != null) submitButton.setDisable(false);
            if (err != null) {
                DataStore.removePending(email);
                errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px;");
                errorLabel.setText("Couldn't send email: " + rootMessage(err));
                return;
            }
            VerifyEmailController.setPendingEmailHint(email);
            SceneManager.getInstance().switchTo("verify-email", true);
        }));
    }

    @FXML
    private void onBack(ActionEvent e) {
        SceneManager.getInstance().switchTo("signin", true);
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
    private void markInvalid(javafx.scene.control.Control c) {
        if (!c.getStyleClass().contains("invalid")) c.getStyleClass().add("invalid");
    }
    private void clearInvalid() {
        nameField.getStyleClass().remove("invalid");
        emailField.getStyleClass().remove("invalid");
        passwordField.getStyleClass().remove("invalid");
        confirmField.getStyleClass().remove("invalid");
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px;");
        errorLabel.setText(" ");
    }

    private static String rootMessage(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null) r = r.getCause();
        return r.getMessage() == null ? r.getClass().getSimpleName() : r.getMessage();
    }
}
