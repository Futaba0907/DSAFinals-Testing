package com.example.dsafinals.controllers;

import com.example.dsafinals.datastructures.Sorter;
import com.example.dsafinals.models.Photo;
import com.example.dsafinals.storage.DataStore;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhotosController {

    @FXML private FlowPane   photoGrid;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private TextField  searchField;
    @FXML private Text       photoCountText;
    @FXML private StackPane  emptyState;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Button     filterButton;
    @FXML private Button     clearFilterButton;

    private final DataStore store = DataStore.getInstance();

    @FXML
    public void initialize() {
        sortComboBox.getItems().addAll(
                "Date (Newest First)", "Date (Oldest First)", "Name (A-Z)");
        sortComboBox.setValue("Date (Newest First)");
        sortComboBox.setOnAction(e -> refreshGrid());

        filterButton.setOnAction(e -> refreshGrid());
        clearFilterButton.setOnAction(e -> {
            fromDatePicker.setValue(null);
            toDatePicker.setValue(null);
            searchField.setText("");
            refreshGrid();
        });

        searchField.setOnKeyReleased(e -> refreshGrid());

        refreshGrid();
    }

    // ─────────────────────────────────────────────
    // GRID
    // ─────────────────────────────────────────────

    private void refreshGrid() {
        photoGrid.getChildren().clear();

        List<Photo> photos = getFilteredSortedPhotos();

        int count = photos.size();
        photoCountText.setText(count + (count == 1 ? " photo" : " photos"));

        boolean isEmpty = photos.isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);

        for (Photo photo : photos) {
            photoGrid.getChildren().add(buildPhotoCard(photo));
        }
    }

    private List<Photo> getFilteredSortedPhotos() {
        List<Photo> photos;

        // Date range filter
        LocalDate from = fromDatePicker.getValue();
        LocalDate to   = toDatePicker.getValue();

        if (from != null && to != null) {
            photos = store.getPhotosInRange(from, to);
        } else {
            photos = new ArrayList<>(store.getPhotos());
        }

        // Keyword filter (file name or caption)
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        if (!keyword.isEmpty()) {
            photos = photos.stream()
                    .filter(p -> p.getFileName().toLowerCase().contains(keyword)
                            || (p.getCaption() != null && p.getCaption().toLowerCase().contains(keyword)))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Sort
        String sort = sortComboBox.getValue();
        if (sort != null) {
            switch (sort) {
                case "Date (Newest First)" -> {
                    Sorter.quickSortPhotosByDate(photos, 0, photos.size() - 1);
                    Collections.reverse(photos);
                }
                case "Date (Oldest First)" -> Sorter.quickSortPhotosByDate(photos, 0, photos.size() - 1);
                case "Name (A-Z)"          -> Sorter.quickSortPhotosByName(photos, 0, photos.size() - 1);
            }
        }
        return photos;
    }

    private VBox buildPhotoCard(Photo photo) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);
        card.setMaxWidth(150);
        card.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 10; -fx-cursor: hand;");
        card.setPadding(new Insets(8));

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
            }
        } catch (Exception ignored) {}

        Label nameLabel = new Label(truncate(photo.getFileName(), 18));
        nameLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");
        nameLabel.setMaxWidth(134);

        Label dateLabel = new Label(photo.getDateTaken().toString());
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");

        card.getChildren().addAll(imageView, nameLabel, dateLabel);
        card.setOnMouseClicked(e -> openPhotoDetail(photo));

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #2a2a2a; -fx-background-radius: 10; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: #1e1e1e; -fx-background-radius: 10; -fx-cursor: hand;"));

        return card;
    }

    // ─────────────────────────────────────────────
    // PHOTO DETAIL DIALOG
    // ─────────────────────────────────────────────

    private void openPhotoDetail(Photo photo) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(photo.getFileName());

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setPrefWidth(420);
        content.setStyle("-fx-background-color: #1a1a1a;");

        // Large preview
        ImageView preview = new ImageView();
        preview.setFitWidth(380);
        preview.setFitHeight(240);
        preview.setPreserveRatio(true);
        try {
            File f = new File(photo.getFilePath());
            if (f.exists()) preview.setImage(new Image(f.toURI().toString()));
        } catch (Exception ignored) {}

        // Caption
        Label captionLabel = styledLabel("Caption");
        TextField captionField = styledTextField(photo.getCaption());

        // Date
        Label dateLabel = styledLabel("Date Taken");
        DatePicker datePicker = new DatePicker(photo.getDateTaken());
        datePicker.setStyle("-fx-background-color: #262626; -fx-text-fill: white;");
        datePicker.setPrefWidth(380);

        // Tags
        Label tagsLabel = styledLabel("Tags (comma separated)");
        TextField tagsField = styledTextField(String.join(", ", photo.getTags()));

        // Delete
        Button deleteBtn = new Button("Delete Photo");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; " +
                           "-fx-font-size: 13px; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Photo");
            confirm.setHeaderText(null);
            confirm.setContentText("Permanently delete this photo?");
            styleDialog(confirm.getDialogPane());
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    store.deletePhoto(photo.getId());
                    dialog.close();
                    refreshGrid();
                }
            });
        });

        content.getChildren().addAll(
                preview, captionLabel, captionField,
                dateLabel, datePicker, tagsLabel, tagsField, deleteBtn);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        styleDialog(dialog.getDialogPane());

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                photo.setCaption(captionField.getText().trim());
                photo.setDateTaken(datePicker.getValue());
                photo.getTags().clear();
                String raw = tagsField.getText().trim();
                if (!raw.isEmpty()) {
                    for (String tag : raw.split(",")) photo.addTag(tag.trim());
                }
                store.saveAll();
                refreshGrid();
            }
        });
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private String truncate(String text, int maxLen) {
        return text.length() > maxLen ? text.substring(0, maxLen - 1) + "…" : text;
    }

    private Label styledLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #93a1a1; -fx-font-size: 12px;");
        return l;
    }

    private TextField styledTextField(String value) {
        TextField tf = new TextField(value == null ? "" : value);
        tf.setStyle("-fx-background-color: #262626; -fx-text-fill: white; " +
                    "-fx-prompt-text-fill: #555; -fx-border-color: #333; " +
                    "-fx-border-radius: 6; -fx-background-radius: 6;");
        tf.setPrefWidth(380);
        return tf;
    }

    private void styleDialog(DialogPane pane) {
        pane.setStyle("-fx-background-color: #1a1a1a;");
        if (pane.lookupButton(ButtonType.OK) != null)
            pane.lookupButton(ButtonType.OK).setStyle(
                    "-fx-background-color: #155dfc; -fx-text-fill: white; -fx-font-weight: bold;");
        if (pane.lookupButton(ButtonType.CANCEL) != null)
            pane.lookupButton(ButtonType.CANCEL).setStyle(
                    "-fx-background-color: #262626; -fx-text-fill: white;");
    }
}
