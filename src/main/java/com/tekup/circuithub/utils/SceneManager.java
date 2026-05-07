package com.tekup.circuithub.utils;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class SceneManager {
    private static SceneManager instance;
    private Stage stage;
    private Scene scene;

    public static SceneManager getInstance() {
        if (instance == null) instance = new SceneManager();
        return instance;
    }

    public void init(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public void switchTo(String viewName, boolean withFade) {
        try {
            URL url = getClass().getResource("/com/tekup/circuithub/views/" + viewName + ".fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            if (scene == null) {
                scene = new Scene(root);
                scene.getStylesheets().add(
                        getClass().getResource("/com/tekup/circuithub/styles/app.css").toExternalForm());
                scene.setFill(javafx.scene.paint.Color.web("#0F172A"));
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            if (withFade) {
                root.setOpacity(0);

                FadeTransition ft = new FadeTransition(Duration.millis(180), root);
                ft.setFromValue(0.0);
                ft.setToValue(1.0);
                ft.setInterpolator(Interpolator.EASE_IN);
                ft.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Stagger-in: each node fades + slides up with a cascading delay.
    public static void staggerIn(List<? extends Node> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            final Node n = nodes.get(i);
            n.setOpacity(0);
            n.setTranslateY(16);
            PauseTransition delay = new PauseTransition(Duration.millis(Math.min(i * 45L, 420)));
            FadeTransition ft = new FadeTransition(Duration.millis(270), n);
            ft.setToValue(1);
            TranslateTransition tt = new TranslateTransition(Duration.millis(270), n);
            tt.setToY(0);
            tt.setInterpolator(Interpolator.EASE_OUT);
            ParallelTransition pt = new ParallelTransition(ft, tt);
            delay.setOnFinished(e -> pt.play());
            delay.play();
        }
    }
}
