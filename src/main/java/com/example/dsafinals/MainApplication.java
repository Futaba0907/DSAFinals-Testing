package com.example.dsafinals;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {

    // Store offsets as class fields
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Journal");
        stage.setScene(scene);

        // Set the minimum bounds for the window
        stage.setMinWidth(800);
        stage.setMinHeight(550);

        // Remove titlebar and make the whole window movable
        Parent root = stage.getScene().getRoot();

        root.setOnMousePressed(event -> {
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        });

        // CSS
        String css = Objects.requireNonNull(getClass().getResource("stylesheets/application.css")).toExternalForm();
        scene.getStylesheets().add(css);

        stage.show();
    }
}
