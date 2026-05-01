package com.tekup.circuithub.controllers;

import com.tekup.circuithub.utils.SceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class WelcomeController {

    @FXML private Label logoLabel;
    @FXML private Button enterButton;
    @FXML private Group circuitOverlay;
    @FXML private StackPane haloLayer;

    @FXML
    public void initialize() {
        // Halo: slow ambient breathing
        if (haloLayer != null) {
            ScaleTransition halo = new ScaleTransition(Duration.seconds(3.6), haloLayer);
            halo.setFromX(0.92); halo.setFromY(0.92);
            halo.setToX(1.08); halo.setToY(1.08);
            halo.setAutoReverse(true);
            halo.setCycleCount(ScaleTransition.INDEFINITE);
            halo.setInterpolator(Interpolator.EASE_BOTH);
            halo.play();

            FadeTransition haloFade = new FadeTransition(Duration.seconds(3.6), haloLayer);
            haloFade.setFromValue(0.7);
            haloFade.setToValue(1.0);
            haloFade.setAutoReverse(true);
            haloFade.setCycleCount(FadeTransition.INDEFINITE);
            haloFade.play();
        }

        // Logo: subtle rise on entrance
        TranslateTransition rise = new TranslateTransition(Duration.millis(600), logoLabel);
        rise.setFromY(12); rise.setToY(0);
        rise.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), logoLabel);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        new ParallelTransition(rise, fadeIn).play();

        // Drifting circuit pattern
        TranslateTransition drift = new TranslateTransition(Duration.seconds(18), circuitOverlay);
        drift.setFromX(-30);
        drift.setToX(30);
        drift.setAutoReverse(true);
        drift.setCycleCount(TranslateTransition.INDEFINITE);
        drift.setInterpolator(Interpolator.EASE_BOTH);
        drift.play();

        // Subtle hover scale on button (CSS handles glow)
        enterButton.setOnMouseEntered(e -> {
            enterButton.setScaleX(1.04);
            enterButton.setScaleY(1.04);
        });
        enterButton.setOnMouseExited(e -> {
            enterButton.setScaleX(1.0);
            enterButton.setScaleY(1.0);
        });
    }

    @FXML
    private void onEnter(ActionEvent e) {
        SceneManager.getInstance().switchTo("signin", true);
    }
}
