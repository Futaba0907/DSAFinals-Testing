package com.example.dsafinals.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;

public class MainController {
    @FXML
    private VBox sidebar;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button journalButton;

    @FXML
    private Button albumsButton;

    @FXML
    private Button photosButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button newEntryButton;

    @FXML
    private Button undoButton;

    @FXML
    private Button redoButton;

    @FXML
    private StackPane contentArea;

    private Button selectedButton;

    @FXML
    public void initialize() {
        selectedButton = dashboardButton;

        // Set icons for each button
        dashboardButton.setGraphic(createIcon("mdi2h-home"));
        journalButton.setGraphic(createIcon("mdi2b-book-open-page-variant"));
        albumsButton.setGraphic(createIcon("mdi2f-folder"));
        photosButton.setGraphic(createIcon("mdi2i-image"));
        settingsButton.setGraphic(createIcon("mdi2c-cog"));
        newEntryButton.setGraphic(createIcon("mdi2p-plus"));

        undoButton.setGraphic(createIcon("mdi2u-undo", "topbar-icon-disabled"));
        redoButton.setGraphic(createIcon("mdi2r-redo", "topbar-icon-disabled"));

        // Change the view when a button is pressed
        bindSidebarButton(dashboardButton, "dashboard.fxml");
        bindSidebarButton(journalButton, "journal.fxml");
        bindSidebarButton(albumsButton, "albums.fxml");
        bindSidebarButton(photosButton, "photos.fxml");

        // Resize the sidebar based on the window width
        sidebar.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                sidebar.prefWidthProperty().bind(newScene.widthProperty().multiply(0.17));
            }
        });
    }

    private void loadPage(String fxml) {
        try {
            URL url = getClass().getResource("/com/example/dsafinals/fxml/" + fxml);
//            System.out.println(url);

            Parent page = FXMLLoader.load(url);

            contentArea.getChildren().setAll(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FontIcon createIcon(String iconCode) {
        FontIcon addIcon = new FontIcon(iconCode);
        addIcon.getStyleClass().add("sidebar-icon");
        addIcon.setIconSize(18);
        return addIcon;
    }

    public FontIcon createIcon(String iconCode, String styleClass) {
        FontIcon addIcon = new FontIcon(iconCode);
        addIcon.getStyleClass().add(styleClass);
        addIcon.setIconSize(18);
        return addIcon;
    }

    public void selectButton(Button button) {
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("selected-button");
        }

        button.getStyleClass().add("selected-button");
        selectedButton = button;
    }

    public void bindSidebarButton(Button button, String fxml) {
        button.setOnAction(e -> {
            loadPage(fxml);
            selectButton(button);
        });
    }
}
