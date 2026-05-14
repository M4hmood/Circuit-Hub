package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.User;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.DataStore.PendingRegistration;
import com.tekup.circuithub.utils.Mailer;
import com.tekup.circuithub.utils.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.UUID;

public class VerifyEmailController {
    @FXML private Label emailLabel;
    @FXML private TextField codeField;
    @FXML private Label statusLabel;
    @FXML private Label errorLabel;
    @FXML private Button verifyButton;
    @FXML private Hyperlink resendLink;

    private String pendingEmail;

    @FXML
    public void initialize() {
        PendingRegistration p = currentPending();
        if (p == null) {
            errorLabel.setText("No pending verification — please sign up again.");
            verifyButton.setDisable(true);
            resendLink.setDisable(true);
            return;
        }
        pendingEmail = p.email;
        emailLabel.setText(p.email);
        statusLabel.setText("Code sent. Check your inbox (and spam folder).");
    }

    private PendingRegistration currentPending() {
        // The signup controller stashes the email on DataStore via putPending; we look it up
        // by walking the most recent pending registration the user just created. Since we
        // don't have a "current pending email" pointer, we rely on the email being passed
        // via the static helper below. See SignUpController.onSignUp.
        return DataStore.getPending(pendingEmailHint);
    }

    // Set by SignUpController before switching to this screen.
    private static String pendingEmailHint;
    public static void setPendingEmailHint(String email) { pendingEmailHint = email; }

    @FXML
    private void onVerify(ActionEvent e) {
        errorLabel.setText(" ");
        PendingRegistration p = DataStore.getPending(pendingEmail);
        if (p == null) {
            errorLabel.setText("Code expired. Please sign up again.");
            verifyButton.setDisable(true);
            return;
        }
        String entered = codeField.getText() == null ? "" : codeField.getText().trim();
        if (!entered.equals(p.code)) {
            errorLabel.setText("Incorrect code. Try again.");
            return;
        }

        User u = new User(UUID.randomUUID().toString(), p.fullName, p.email, p.passwordHash);
        DataStore.addUser(u);
        DataStore.removePending(p.email);
        DataStore.setCurrentUser(u);
        SceneManager.getInstance().switchTo("dashboard", true);
    }

    @FXML
    private void onResend(ActionEvent e) {
        errorLabel.setText(" ");
        PendingRegistration prev = DataStore.getPending(pendingEmail);
        if (prev == null) {
            errorLabel.setText("Session expired. Please sign up again.");
            verifyButton.setDisable(true);
            return;
        }
        String newCode = DataStore.generateVerificationCode();
        DataStore.putPending(prev.fullName, prev.email, prev.passwordHash, newCode);
        statusLabel.setText("Sending new code…");
        resendLink.setDisable(true);
        Mailer.sendVerificationCode(prev.email, newCode)
                .whenComplete((ok, err) -> Platform.runLater(() -> {
                    resendLink.setDisable(false);
                    if (err != null) {
                        statusLabel.setText(" ");
                        errorLabel.setText("Couldn't send email: " + rootMessage(err));
                    } else {
                        statusLabel.setText("New code sent.");
                    }
                }));
    }

    @FXML
    private void onBack(ActionEvent e) {
        if (pendingEmail != null) DataStore.removePending(pendingEmail);
        SceneManager.getInstance().switchTo("signup", true);
    }

    private static String rootMessage(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null) r = r.getCause();
        return r.getMessage() == null ? r.getClass().getSimpleName() : r.getMessage();
    }
}
