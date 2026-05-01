package com.tekup.circuithub;

import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.Fonts;
import com.tekup.circuithub.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        Fonts.loadAll();
        DataStore.init();
        SceneManager.getInstance().init(stage);
        SceneManager.getInstance().switchTo("welcome", false);
        stage.setTitle("CircuitHub — Power Your Next Build");
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
