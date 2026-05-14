module com.tekup.circuithub {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires java.sql;
    requires jakarta.mail;

    opens com.tekup.circuithub to javafx.fxml;
    opens com.tekup.circuithub.controllers to javafx.fxml;

    exports com.tekup.circuithub;
    exports com.tekup.circuithub.controllers;
    exports com.tekup.circuithub.models;
    exports com.tekup.circuithub.utils;
}
