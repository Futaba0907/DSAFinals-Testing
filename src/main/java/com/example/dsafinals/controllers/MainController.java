package com.example.dsafinals.controllers;

import com.example.dsafinals.action.Action;
import com.example.dsafinals.action.ActionManager;
import com.example.dsafinals.io.FileManager;
import com.example.dsafinals.model.AlbumNode;
import com.example.dsafinals.model.JournalEntry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;

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

    private final ActionManager actionManager = new ActionManager();
    private AlbumNode library;
    private FontIcon undoIcon;
    private FontIcon redoIcon;

    @FXML
    public void initialize() {
        selectedButton = dashboardButton;
        library = loadLibrary();

        dashboardButton.setGraphic(createIcon("mdi2h-home"));
        journalButton.setGraphic(createIcon("mdi2b-book-open-page-variant"));
        albumsButton.setGraphic(createIcon("mdi2f-folder"));
        photosButton.setGraphic(createIcon("mdi2i-image"));
        settingsButton.setGraphic(createIcon("mdi2c-cog"));
        newEntryButton.setGraphic(createIcon("mdi2p-plus"));

        undoIcon = createIcon("mdi2u-undo", "topbar-icon-disabled");
        redoIcon = createIcon("mdi2r-redo", "topbar-icon-disabled");
        undoButton.setGraphic(undoIcon);
        redoButton.setGraphic(redoIcon);

        bindSidebarButton(dashboardButton, "dashboard.fxml");
        bindSidebarButton(journalButton, "journal.fxml");
        bindSidebarButton(albumsButton, "albums.fxml");
        bindSidebarButton(photosButton, "photos.fxml");

        newEntryButton.setOnAction(e -> handleNewEntry());
        undoButton.setOnAction(e -> {
            actionManager.undo();
            refreshUndoRedoState();
            saveLibrary();
        });
        redoButton.setOnAction(e -> {
            actionManager.redo();
            refreshUndoRedoState();
            saveLibrary();
        });
        refreshUndoRedoState();

        sidebar.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                sidebar.prefWidthProperty().bind(newScene.widthProperty().multiply(0.17));
            }
        });
    }

    private void handleNewEntry() {
        try {
            URL url = getClass().getResource("/com/example/dsafinals/fxml/new-entry-dialog.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Parent form = loader.load();
            NewEntryController formController = loader.getController();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("New Entry");
            dialog.setScene(new Scene(form));
            dialog.showAndWait();

            if (!formController.isSaved()) return;

            JournalEntry entry = new JournalEntry(formController.getTitle(), formController.getContent(),
                    LocalDate.now(), formController.getTags(), new String[0]);
            Action addEntryAction = new Action() {
                @Override
                public void redo() {
                    library.addEntry(entry);
                }

                @Override
                public void undo() {
                    library.removeEntry(entry);
                }
            };
            actionManager.perform(addEntryAction);
            refreshUndoRedoState();
            saveLibrary();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshUndoRedoState() {
        setIconState(undoButton, undoIcon, actionManager.canUndo());
        setIconState(redoButton, redoIcon, actionManager.canRedo());
    }

    private void setIconState(Button button, FontIcon icon, boolean enabled) {
        button.setDisable(!enabled);
        icon.getStyleClass().removeAll("topbar-icon-disabled", "topbar-icon-available");
        icon.getStyleClass().add(enabled ? "topbar-icon-available" : "topbar-icon-disabled");
    }

    private AlbumNode loadLibrary() {
        try {
            AlbumNode loaded = FileManager.loadLibrary();
            if (loaded != null) return loaded;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not load library: " + e.getMessage());
        }
        return new AlbumNode("Library");
    }

    private void saveLibrary() {
        try {
            FileManager.saveLibrary(library);
        } catch (IOException e) {
            System.err.println("Could not save library: " + e.getMessage());
        }
    }

    private void loadPage(String fxml) {
        try {
            URL url = getClass().getResource("/com/example/dsafinals/fxml/" + fxml);
            FXMLLoader loader = new FXMLLoader(url);
            Parent page = loader.load();
            Object controller = loader.getController();
            if (controller instanceof LibraryAware libraryAware) {
                libraryAware.setLibrary(library);
            }
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FontIcon createIcon(String iconCode) {
        FontIcon icon = new FontIcon(iconCode);
        icon.getStyleClass().add("sidebar-icon");
        icon.setIconSize(18);
        return icon;
    }

    public FontIcon createIcon(String iconCode, String styleClass) {
        FontIcon icon = new FontIcon(iconCode);
        icon.getStyleClass().add(styleClass);
        icon.setIconSize(18);
        return icon;
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
