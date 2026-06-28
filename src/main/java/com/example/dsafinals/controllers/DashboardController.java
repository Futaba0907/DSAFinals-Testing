package com.example.dsafinals.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;

public class DashboardController {
    @FXML
    private ScrollPane scrollPane;

    @FXML
    public void initialize() {
        // you can safely use it here
        scrollPane.setPannable(true);
        scrollPane.setFocusTraversable(false);
        scrollPane.setOnMouseClicked(e -> scrollPane.requestFocus());

        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
    }
}
