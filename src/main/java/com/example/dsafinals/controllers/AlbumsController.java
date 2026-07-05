package com.example.dsafinals.controllers;

import com.example.dsafinals.datastructures.AlbumTree;
import com.example.dsafinals.datastructures.Sorter;
import com.example.dsafinals.models.Album;
import com.example.dsafinals.models.Photo;
import com.example.dsafinals.storage.DataStore;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AlbumsController {

    @FXML private TreeView<String> albumTreeView;
    @FXML private Button newAlbumButton;
    @FXML private Button deleteAlbumButton;
    @FXML private Button importPhotoButton;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private FlowPane photoGrid;
    @FXML private Text albumNameText;
    @FXML private Text photoCountText;
    @FXML private StackPane emptyState;

    private final DataStore store = DataStore.getInstance();
    private Album selectedAlbum = null;

    @FXML
    public void initialize() {
        // Sort options
        sortComboBox.getItems().addAll("Date (Newest First)", "Date (Oldest First)", "Name (A-Z)");
        sortComboBox.setValue("Date (Newest First)");
        sortComboBox.setOnAction(e -> refreshPhotoGrid());

        // Build the album tree
        refreshAlbumTree();

        // Buttons
        newAlbumButton.setOnAction(e -> handleNewAlbum());
        deleteAlbumButton.setOnAction(e -> handleDeleteAlbum());
        importPhotoButton.setOnAction(e -> handleImportPhoto());

        // Disable import/delete until an album is selected
        importPhotoButton.setDisable(true);
        deleteAlbumButton.setDisable(true);

        // Listen for album selection
        albumTreeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onAlbumSelected(newVal)
        );
    }
    // Album Tree

    private void refreshAlbumTree() {
        TreeItem<String> root = new TreeItem<>("Albums");
        root.setExpanded(true);

        for (AlbumTree.AlbumNode node : store.getAlbumTree().getRootAlbums()) {
            root.getChildren().add(buildTreeItem(node));
        }

        albumTreeView.setRoot(root);
        albumTreeView.setShowRoot(false);
        styleTreeView();
    }

    private TreeItem<String> buildTreeItem(AlbumTree.AlbumNode node) {
        TreeItem<String> item = new TreeItem<>("📁  " + node.album.getName());
        item.setExpanded(true);
        // Store album ID in the item's user data via a tag approach
        for (AlbumTree.AlbumNode child : node.children) {
            item.getChildren().add(buildTreeItem(child));
        }
        return item;
    }

    private void styleTreeView() {
        albumTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                             "-fx-font-size: 14px; -fx-padding: 6 8;");
                }
            }
        });
    }

    private void onAlbumSelected(TreeItem<String> item) {
        if (item == null || item.getValue() == null) return;

        // Extract album name
        String rawName = item.getValue().replace("📁  ", "").trim();

        // Find matching album in the store
        selectedAlbum = findAlbumByName(rawName);

        if (selectedAlbum != null) {
            albumNameText.setText(selectedAlbum.getName());
            importPhotoButton.setDisable(false);
            deleteAlbumButton.setDisable(false);
            refreshPhotoGrid();
        }
    }

    private Album findAlbumByName(String name) {
        for (Album a : store.getAlbumTree().toFlatList()) {
            if (a.getName().equals(name)) return a;
        }
        return null;
    }

    // NEW/DELETE Album


    private void handleNewAlbum() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Album");
        dialog.setHeaderText(null);
        dialog.setContentText("Album name:");
        styleDialog(dialog.getDialogPane());

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                // If an album is selected, create as sub-album; otherwise root level
                String parentId = selectedAlbum != null ? selectedAlbum.getId() : null;
                Album newAlbum = new Album(name.trim(), parentId);
                store.addAlbum(newAlbum);
                refreshAlbumTree();
            }
        });
    }

    private void handleDeleteAlbum() {
        if (selectedAlbum == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Album");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete \"" + selectedAlbum.getName() + "\"? Photos will not be deleted.");
        styleDialog(confirm.getDialogPane());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                store.deleteAlbum(selectedAlbum.getId());
                selectedAlbum = null;
                albumNameText.setText("Select an album");
                photoCountText.setText("");
                photoGrid.getChildren().clear();
                importPhotoButton.setDisable(true);
                deleteAlbumButton.setDisable(true);
                refreshAlbumTree();
            }
        });
    }

    // PHOTO GRID

    private void refreshPhotoGrid() {
        photoGrid.getChildren().clear();

        if (selectedAlbum == null) return;

        // Get photos belonging to this album
        List<Photo> albumPhotos = getPhotosForAlbum(selectedAlbum);

        // Sort based on combo selection
        albumPhotos = applySorting(albumPhotos);

        // Update count
        int count = albumPhotos.size();
        photoCountText.setText(count + (count == 1 ? " photo" : " photos"));

        // Toggle empty state
        boolean isEmpty = albumPhotos.isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);

        // Build thumbnail cards
        for (Photo photo : albumPhotos) {
            photoGrid.getChildren().add(buildPhotoCard(photo));
        }
    }

    private List<Photo> getPhotosForAlbum(Album album) {
        List<Photo> result = new ArrayList<>();
        for (String photoId : album.getPhotoIds()) {
            Photo p = store.findPhotoById(photoId);
            if (p != null) result.add(p);
        }
        return result;
    }

    private List<Photo> applySorting(List<Photo> photos) {
        String sort = sortComboBox.getValue();
        List<Photo> sorted = new ArrayList<>(photos);
        if (sort == null) return sorted;

        switch (sort) {
            case "Date (Newest First)" -> {
                Sorter.quickSortPhotosByDate(sorted, 0, sorted.size() - 1);
                java.util.Collections.reverse(sorted);
            }
            case "Date (Oldest First)" -> Sorter.quickSortPhotosByDate(sorted, 0, sorted.size() - 1);
            case "Name (A-Z)" -> Sorter.quickSortPhotosByName(sorted, 0, sorted.size() - 1);
        }
        return sorted;
    }

    private VBox buildPhotoCard(Photo photo) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);
        card.setMaxWidth(150);
        card.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 10; -fx-cursor: hand;");
        card.setPadding(new Insets(8));

        // Thumbnail
        ImageView imageView = new ImageView();
        imageView.setFitWidth(134);
        imageView.setFitHeight(110);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        try {
            File imgFile = new File(photo.getFilePath());
            if (imgFile.exists()) {
                Image img = new Image(imgFile.toURI().toString(), 134, 110, false, true, true);
                imageView.setImage(img);
            } else {
                imageView.setStyle("-fx-background-color: #333;");
            }
        } catch (Exception e) {
            imageView.setStyle("-fx-background-color: #333;");
        }

        // File name label
        Label nameLabel = new Label(truncate(photo.getFileName(), 18));
        nameLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");
        nameLabel.setMaxWidth(134);

        // Date label
        Label dateLabel = new Label(photo.getDateTaken().toString());
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");

        card.getChildren().addAll(imageView, nameLabel, dateLabel);

        // Click open detail dialog
        card.setOnMouseClicked(e -> openPhotoDetail(photo));

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #2a2a2a; -fx-background-radius: 10; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: #1e1e1e; -fx-background-radius: 10; -fx-cursor: hand;"));

        return card;
    }

    // IMPORT PHOTO

    private void handleImportPhoto() {
        if (selectedAlbum == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        List<File> files = chooser.showOpenMultipleDialog(importPhotoButton.getScene().getWindow());
        if (files == null || files.isEmpty()) return;

        for (File file : files) {
            // Use today's date as default; user can edit it in the detail view
            Photo photo = store.importPhoto(file.getAbsolutePath(), LocalDate.now());
            if (photo != null) {
                selectedAlbum.addPhoto(photo.getId());
            }
        }

        // Save updated album
        store.saveAll();
        refreshPhotoGrid();
    }


    // PHOTO DETAILS


    private void openPhotoDetail(Photo photo) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(photo.getFileName());

        // Layout
        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setPrefWidth(420);
        content.setStyle("-fx-background-color: #1a1a1a;");

        // Large image preview
        ImageView preview = new ImageView();
        preview.setFitWidth(380);
        preview.setFitHeight(220);
        preview.setPreserveRatio(true);
        try {
            File f = new File(photo.getFilePath());
            if (f.exists()) preview.setImage(new Image(f.toURI().toString()));
        } catch (Exception ignored) {}

        // Caption field
        Label captionLabel = styledLabel("Caption");
        TextField captionField = styledTextField(photo.getCaption());

        // Date field
        Label dateLabel = styledLabel("Date Taken");
        DatePicker datePicker = new DatePicker(photo.getDateTaken());
        datePicker.setStyle("-fx-background-color: #262626; -fx-text-fill: white;");
        datePicker.setPrefWidth(380);

        // Tags field
        Label tagsLabel = styledLabel("Tags (comma separated)");
        TextField tagsField = styledTextField(String.join(", ", photo.getTags()));

        // Delete button
        Button deleteBtn = new Button("Delete Photo");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; " +
                           "-fx-font-size: 13px; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            if (selectedAlbum != null) {
                selectedAlbum.removePhoto(photo.getId());
            }
            store.deletePhoto(photo.getId());
            dialog.close();
            refreshPhotoGrid();
        });

        content.getChildren().addAll(
                preview, captionLabel, captionField,
                dateLabel, datePicker, tagsLabel, tagsField, deleteBtn
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        styleDialog(dialog.getDialogPane());

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Apply edits
                photo.setCaption(captionField.getText().trim());
                photo.setDateTaken(datePicker.getValue());

                // Parse tags
                photo.getTags().clear();
                String tagsRaw = tagsField.getText().trim();
                if (!tagsRaw.isEmpty()) {
                    for (String tag : tagsRaw.split(",")) {
                        photo.addTag(tag.trim());
                    }
                }

                store.saveAll();
                refreshPhotoGrid();
            }
        });
    }


    // Functions for styling

    private String truncate(String text, int maxLen) {
        return text.length() > maxLen ? text.substring(0, maxLen - 1) + "…" : text;
    }

    private Label styledLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #93a1a1; -fx-font-size: 12px;");
        return l;
    }

    private TextField styledTextField(String value) {
        TextField tf = new TextField(value);
        tf.setStyle("-fx-background-color: #262626; -fx-text-fill: white; " +
                    "-fx-prompt-text-fill: #555; -fx-border-color: #333; -fx-border-radius: 6; " +
                    "-fx-background-radius: 6;");
        tf.setPrefWidth(380);
        return tf;
    }

    private void styleDialog(DialogPane pane) {
        pane.setStyle("-fx-background-color: #1a1a1a;");
        pane.lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: #155dfc; -fx-text-fill: white; -fx-font-weight: bold;");
        if (pane.lookupButton(ButtonType.CANCEL) != null) {
            pane.lookupButton(ButtonType.CANCEL).setStyle(
                    "-fx-background-color: #262626; -fx-text-fill: white;");
        }
    }
}
