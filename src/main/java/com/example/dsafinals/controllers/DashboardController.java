package com.example.dsafinals.controllers;

import com.example.dsafinals.models.JournalEntry;
import com.example.dsafinals.models.Photo;
import com.example.dsafinals.storage.DataStore;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private ScrollPane scrollPane;

    // Stats
    @FXML private Label entryCountLabel;
    @FXML private Label photoCountLabel;
    @FXML private Label albumCountLabel;

    // On This Day
    @FXML private VBox  onThisDaySection;
    @FXML private VBox  onThisDayEntries;
    @FXML private FlowPane onThisDayPhotos;
    @FXML private Text  onThisDayEmptyText;

    // Recent entries
    @FXML private VBox recentEntriesContainer;
    @FXML private Text recentEntriesEmpty;

    private final DataStore store = DataStore.getInstance();

    /** Set by MainController so entry cards can navigate to the Journal page. */
    private java.util.function.Consumer<JournalEntry> entryClickHandler;

    public void setEntryClickHandler(java.util.function.Consumer<JournalEntry> handler) {
        this.entryClickHandler = handler;
    }

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy");

    @FXML
    public void initialize() {
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setFocusTraversable(false);
        scrollPane.setOnMouseClicked(e -> scrollPane.requestFocus());

        refresh();
    }

    public void refresh() {
        refreshStats();
        refreshOnThisDay();
        refreshRecentEntries();
    }

    // STATS CARDS

    private void refreshStats() {
        entryCountLabel.setText(String.valueOf(store.getEntries().size()));
        photoCountLabel.setText(String.valueOf(store.getPhotos().size()));
        albumCountLabel.setText(String.valueOf(store.getAlbumTree().toFlatList().size()));
    }

    // ON THIS DAY

    private void refreshOnThisDay() {
        onThisDayEntries.getChildren().clear();
        onThisDayPhotos.getChildren().clear();

        List<JournalEntry> pastEntries = store.getOnThisDayEntries();
        List<Photo>        pastPhotos  = store.getOnThisDayPhotos();

        boolean empty = pastEntries.isEmpty() && pastPhotos.isEmpty();
        onThisDayEmptyText.setVisible(empty);
        onThisDayEmptyText.setManaged(empty);

        for (JournalEntry entry : pastEntries) {
            onThisDayEntries.getChildren().add(buildMemoryEntryCard(entry));
        }

        for (Photo photo : pastPhotos) {
            onThisDayPhotos.getChildren().add(buildMemoryPhotoThumb(photo));
        }
    }

    private HBox buildMemoryEntryCard(JournalEntry entry) {
        HBox card = new HBox(14);
        card.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 12; -fx-cursor: hand;");
        card.setPadding(new Insets(16));
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #262626; -fx-background-radius: 12; -fx-cursor: hand;"));
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 12; -fx-cursor: hand;"));
        card.setOnMouseClicked(e -> {
            if (entryClickHandler != null) entryClickHandler.accept(entry);
        });

        // Year badge
        VBox badge = new VBox();
        badge.setAlignment(Pos.CENTER);
        badge.setMinWidth(60);
        badge.setStyle("-fx-background-color: #155dfc; -fx-background-radius: 8;");
        badge.setPadding(new Insets(8));
        long yearsAgo = LocalDate.now().getYear() - entry.getDate().getYear();
        Label yearNum = new Label(String.valueOf(entry.getDate().getYear()));
        yearNum.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label yearsAgoLabel = new Label(yearsAgo + (yearsAgo == 1 ? " yr ago" : " yrs ago"));
        yearsAgoLabel.setStyle("-fx-text-fill: #ccd; -fx-font-size: 10px;");
        badge.getChildren().addAll(yearNum, yearsAgoLabel);

        VBox text = new VBox(5);
        HBox.setHgrow(text, Priority.ALWAYS);

        String displayTitle = (entry.getTitle() == null || entry.getTitle().isBlank())
                ? "Untitled" : entry.getTitle();
        Label title = new Label(displayTitle);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

        String snippetText = entry.getContent() == null ? "" :
                entry.getContent().replaceAll("\\s+", " ").trim();
        if (snippetText.length() > 120) snippetText = snippetText.substring(0, 120) + "…";
        Label snippet = new Label(snippetText);
        snippet.setStyle("-fx-text-fill: #93a1a1; -fx-font-size: 13px;");
        snippet.setWrapText(true);

        text.getChildren().addAll(title, snippet);
        card.getChildren().addAll(badge, text);
        return card;
    }

    private StackPane buildMemoryPhotoThumb(Photo photo) {
        StackPane container = new StackPane();
        container.setPrefSize(130, 110);
        container.setStyle("-fx-background-color: #262626; -fx-background-radius: 10;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(130);
        imageView.setFitHeight(110);
        imageView.setPreserveRatio(false);
        try {
            File f = new File(photo.getFilePath());
            if (f.exists())
                imageView.setImage(new Image(f.toURI().toString(), 130, 110, false, true, true));
        } catch (Exception ignored) {}

        // Year overlay
        Label yearBadge = new Label(String.valueOf(photo.getDateTaken().getYear()));
        yearBadge.setStyle("-fx-background-color: rgba(0,0,0,0.65); -fx-text-fill: white; " +
                           "-fx-font-size: 11px; -fx-padding: 2 6; -fx-background-radius: 6;");
        StackPane.setAlignment(yearBadge, Pos.BOTTOM_LEFT);
        StackPane.setMargin(yearBadge, new Insets(0, 0, 6, 6));

        container.getChildren().addAll(imageView, yearBadge);
        return container;
    }

    // RECENT ENTRIES

    private void refreshRecentEntries() {
        recentEntriesContainer.getChildren().clear();

        List<JournalEntry> sorted = store.getEntriesSortedByDate();
        java.util.Collections.reverse(sorted); // newest first

        boolean isEmpty = sorted.isEmpty();
        recentEntriesEmpty.setVisible(isEmpty);
        recentEntriesEmpty.setManaged(isEmpty);

        // Show up to 5 most recent
        int limit = Math.min(5, sorted.size());
        for (int i = 0; i < limit; i++) {
            recentEntriesContainer.getChildren().add(buildRecentEntryRow(sorted.get(i)));
        }
    }

    private HBox buildRecentEntryRow(JournalEntry entry) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14));
        row.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 10; -fx-cursor: hand;");
        row.setOnMouseEntered(e ->
                row.setStyle("-fx-background-color: #262626; -fx-background-radius: 10; -fx-cursor: hand;"));
        row.setOnMouseExited(e ->
                row.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 10; -fx-cursor: hand;"));
        row.setOnMouseClicked(e -> {
            if (entryClickHandler != null) entryClickHandler.accept(entry);
        });

        VBox text = new VBox(4);
        HBox.setHgrow(text, Priority.ALWAYS);

        String displayTitle = (entry.getTitle() == null || entry.getTitle().isBlank())
                ? "Untitled" : entry.getTitle();
        Label title = new Label(displayTitle);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        String snippetText = entry.getContent() == null ? "" :
                entry.getContent().replaceAll("\\s+", " ").trim();
        if (snippetText.length() > 80) snippetText = snippetText.substring(0, 80) + "…";
        Label snippet = new Label(snippetText);
        snippet.setStyle("-fx-text-fill: #93a1a1; -fx-font-size: 12px;");

        text.getChildren().addAll(title, snippet);

        Label date = new Label(entry.getDate() != null ? entry.getDate().format(DATE_FMT) : "");
        date.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

        row.getChildren().addAll(text, date);
        return row;
    }
}
