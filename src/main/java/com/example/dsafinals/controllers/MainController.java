package com.example.dsafinals.controllers;

import com.example.dsafinals.storage.DataStore;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import com.example.dsafinals.controllers.JournalController;

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

    private final DataStore store = DataStore.getInstance();

    /** Tracks the fxml file currently shown in contentArea, so undo/redo can refresh it. */
    private String currentFxml = "dashboard.fxml";

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

        // Undo / Redo actions
        undoButton.setOnAction(e -> {
            store.undo();
            refreshCurrentPage();
        });
        redoButton.setOnAction(e -> {
            store.redo();
            refreshCurrentPage();
        });

        // Keep the undo/redo buttons live: fires instantly whenever ANY page
        // (Journal, Photos, Albums, etc.) adds/edits/deletes something, or
        // when undo/redo itself runs — no need to wait for navigation.
        store.addStackChangeListener(this::refreshUndoRedoButtons);
        refreshUndoRedoButtons();

        // Change the view when a button is pressed
        dashboardButton.setOnAction(e -> {
            selectButton(dashboardButton);
            openDashboard();
        });
        newEntryButton.setOnAction(e -> {
            selectButton(journalButton);
            JournalController controller = (JournalController) loadPageAndGetController("journal.fxml");
            if (controller != null) controller.openEditor(null);
        });
        bindSidebarButton(journalButton, "journal.fxml");
        bindSidebarButton(albumsButton, "albums.fxml");
        bindSidebarButton(photosButton, "photos.fxml");
        bindSidebarButton(settingsButton, "settings.fxml");

        // Resize the sidebar based on the window width
        sidebar.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                sidebar.prefWidthProperty().bind(newScene.widthProperty().multiply(0.17));
            }
        });
        openDashboard();
    }

    /** Loads the Dashboard and wires its entry cards so clicking one jumps to the Journal page. */
    private void openDashboard() {
        DashboardController controller = (DashboardController) loadPageAndGetController("dashboard.fxml");
        if (controller != null) {
            controller.setEntryClickHandler(entry -> {
                selectButton(journalButton);
                JournalController journalController =
                        (JournalController) loadPageAndGetController("journal.fxml");
                if (journalController != null) journalController.openEntry(entry);
            });
        }
    }

    private Object loadPageAndGetController(String fxml) {
        try {
            URL url = getClass().getResource("/com/example/dsafinals/fxml/" + fxml);
            FXMLLoader loader = new FXMLLoader(url);
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);
            currentFxml = fxml;
            refreshUndoRedoButtons();
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadPage(String fxml) {
        try {
            URL url = getClass().getResource("/com/example/dsafinals/fxml/" + fxml);
//            System.out.println(url);

            Parent page = FXMLLoader.load(url);

            contentArea.getChildren().setAll(page);
            currentFxml = fxml;
            refreshUndoRedoButtons();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Reload whichever page is currently showing, so undo/redo changes are reflected. */
    private void refreshCurrentPage() {
        if ("dashboard.fxml".equals(currentFxml)) {
            openDashboard();
        } else {
            loadPageAndGetController(currentFxml);
        }
    }

    /** Enable/disable the undo/redo buttons and swap their icon color based on stack state. */
    private void refreshUndoRedoButtons() {
        boolean canUndo = store.canUndo();
        boolean canRedo = store.canRedo();

        undoButton.setDisable(!canUndo);
        undoButton.setGraphic(createIcon("mdi2u-undo", canUndo ? "topbar-icon-available" : "topbar-icon-disabled"));

        redoButton.setDisable(!canRedo);
        redoButton.setGraphic(createIcon("mdi2r-redo", canRedo ? "topbar-icon-available" : "topbar-icon-disabled"));
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
