package com.example.dsafinals.controllers;

import com.example.dsafinals.models.JournalEntry;
import com.example.dsafinals.models.Photo;
import com.example.dsafinals.storage.DataStore;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JournalController {

    // Sidebar (entry list)
    @FXML private TextField searchField;
    @FXML private Button    newEntryButton;
    @FXML private ScrollPane listScrollPane;
    @FXML private VBox      groupedListContainer;
    @FXML private StackPane emptyState;

    // Detail / editor
    @FXML private VBox      detailEmptyState;
    @FXML private ScrollPane editorScrollPane;
    @FXML private Text      dateTimeText;
    @FXML private Button    deleteButton;
    @FXML private Button    saveButton;
    @FXML private TextField titleField;
    @FXML private DatePicker datePicker;
    @FXML private TextField tagInputField;
    @FXML private FlowPane  tagsFlowPane;
    @FXML private TextArea  contentArea;
    @FXML private FlowPane  photosFlowPane;
    @FXML private Button    addPhotoButton;

    private final DataStore store = DataStore.getInstance();
    private JournalEntry currentEntry = null;   // null → drafting a new, unsaved entry
    private final List<String> currentPhotoPaths = new ArrayList<>();

    private static final DateTimeFormatter MONTH_HEADER_FMT =
            DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter DETAIL_HEADER_FMT =
            DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("h:mm a");

    // Button Functions

    @FXML
    public void initialize() {
        newEntryButton.setOnAction(e -> openEditor(null));
        saveButton.setOnAction(e -> handleSave());
        deleteButton.setOnAction(e -> handleDelete());
        addPhotoButton.setOnAction(e -> handleAddPhoto());
        searchField.setOnKeyReleased(e -> refreshEntryList());

        tagInputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) addTagFromInput();
        });

        refreshEntryList();
        showEmptyDetail();
    }

    // ENTRY LIST — grouped by month, newest first

    private void refreshEntryList() {
        groupedListContainer.getChildren().clear();

        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        List<JournalEntry> entries = keyword.isEmpty()
                ? store.getEntriesSortedByDate()
                : store.searchEntriesByKeyword(keyword);

        // Sort newest first (by date, then by creation time within the same date)
        List<JournalEntry> sorted = new ArrayList<>(entries);
        sorted.sort((a, b) -> {
            int byDate = b.getDate().compareTo(a.getDate());
            if (byDate != 0) return byDate;
            return Long.compare(createdMillis(b), createdMillis(a));
        });

        boolean isEmpty = sorted.isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);

        YearMonth currentGroup = null;
        for (JournalEntry entry : sorted) {
            YearMonth ym = YearMonth.from(entry.getDate());
            if (!ym.equals(currentGroup)) {
                currentGroup = ym;
                groupedListContainer.getChildren().add(buildMonthHeader(ym));
            }
            groupedListContainer.getChildren().add(buildEntryRow(entry));
        }
    }

    private Text buildMonthHeader(YearMonth ym) {
        Text header = new Text(ym.format(MONTH_HEADER_FMT));
        header.setFill(javafx.scene.paint.Color.web("#93a1a1"));
        header.setFont(Font.font("System", FontWeight.BOLD, 13));
        VBox.setMargin(header, new Insets(14, 18, 6, 18));
        return header;
    }

    private HBox buildEntryRow(JournalEntry entry) {
        boolean selected = currentEntry != null && currentEntry.getId().equals(entry.getId());

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 18, 10, 18));
        row.setStyle(rowStyle(selected));
        row.setOnMouseEntered(e -> { if (!isSelected(entry)) row.setStyle(rowStyle(false, true)); });
        row.setOnMouseExited(e -> row.setStyle(rowStyle(isSelected(entry))));
        row.setOnMouseClicked(e -> openEditor(entry));

        // Weekday + day of month badge
        VBox dateBadge = new VBox(0);
        dateBadge.setAlignment(Pos.CENTER);
        dateBadge.setMinWidth(38);
        Label weekday = new Label(entry.getDate().getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase());
        weekday.setStyle("-fx-text-fill: #93a1a1; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label dayNum = new Label(String.format("%02d", entry.getDate().getDayOfMonth()));
        dayNum.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        dateBadge.getChildren().addAll(weekday, dayNum);

        // Title / snippet / time column
        VBox textCol = new VBox(3);
        HBox.setHgrow(textCol, Priority.ALWAYS);

        String displayTitle = (entry.getTitle() == null || entry.getTitle().isBlank())
                ? "Untitled" : entry.getTitle();
        Label title = new Label(displayTitle);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        title.setMaxWidth(Double.MAX_VALUE);

        String snippetText = entry.getContent() == null ? "" :
                entry.getContent().replaceAll("\\s+", " ").trim();
        if (snippetText.length() > 70) snippetText = snippetText.substring(0, 70) + "…";
        Label snippet = new Label(snippetText);
        snippet.setStyle("-fx-text-fill: #93a1a1; -fx-font-size: 12px;");
        snippet.setWrapText(true);
        snippet.setMaxWidth(190);

        Label time = new Label(createdDateTime(entry).format(TIME_FMT));
        time.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

        textCol.getChildren().addAll(title, snippet, time);

        row.getChildren().addAll(dateBadge, textCol);

        // Thumbnail, if this entry has photos attached
        if (!entry.getPhotoPaths().isEmpty()) {
            ImageView thumb = new ImageView();
            thumb.setFitWidth(46);
            thumb.setFitHeight(46);
            thumb.setPreserveRatio(false);
            thumb.setSmooth(true);
            try {
                File f = new File(entry.getPhotoPaths().get(0));
                if (f.exists()) thumb.setImage(new Image(f.toURI().toString(), 46, 46, false, true, true));
            } catch (Exception ignored) {}

            StackPane thumbHolder = new StackPane(thumb);
            thumbHolder.setStyle("-fx-background-color: #262626; -fx-background-radius: 8;");
            thumbHolder.setPrefSize(46, 46);
            thumbHolder.setMaxSize(46, 46);
            row.getChildren().add(thumbHolder);
        }

        return row;
    }

    private boolean isSelected(JournalEntry entry) {
        return currentEntry != null && currentEntry.getId().equals(entry.getId());
    }

    private String rowStyle(boolean selected) {
        return rowStyle(selected, false);
    }

    private String rowStyle(boolean selected, boolean hover) {
        String bg = selected ? "#1447e6" : (hover ? "#232323" : "transparent");
        return "-fx-background-color: " + bg + "; -fx-background-radius: 10; -fx-cursor: hand;";
    }


    // DETAILS/EDITOR


    public void openEditor(JournalEntry entry) {
        currentEntry = entry;
        currentPhotoPaths.clear();

        if (entry != null) {
            titleField.setText(entry.getTitle());
            datePicker.setValue(entry.getDate() != null ? entry.getDate() : LocalDate.now());
            contentArea.setText(entry.getContent());
            currentPhotoPaths.addAll(entry.getPhotoPaths());
            dateTimeText.setText(formatHeaderDateTime(entry));
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
        } else {
            titleField.setText("");
            datePicker.setValue(LocalDate.now());
            contentArea.setText("");
            dateTimeText.setText(LocalDateTime.now().format(DETAIL_HEADER_FMT) +
                    " at " + LocalDateTime.now().format(TIME_FMT));
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }

        tagInputField.setText("");
        renderTagsEditor();
        renderPhotosEditor();
        showEditorPane();
        refreshEntryList();
        titleField.requestFocus();
    }

    // Used by external callers to jump straight to an entry.
    public void openEntry(JournalEntry entry) {
        openEditor(entry);
    }

    private String formatHeaderDateTime(JournalEntry entry) {
        String datePart = entry.getDate() != null
                ? entry.getDate().format(DETAIL_HEADER_FMT)
                : "";
        String timePart = createdDateTime(entry).format(TIME_FMT);
        return datePart + " at " + timePart;
    }

    private void renderTagsEditor() {
        tagsFlowPane.getChildren().clear();
        List<String> tags = currentEntry != null
                ? currentEntry.getTags()
                : new ArrayList<>();

        for (String tag : tags) {
            String finalTag = tag;
            tagsFlowPane.getChildren().add(buildTagChip(tag, () -> {
                if (currentEntry != null) currentEntry.removeTag(finalTag);
                renderTagsEditor();
            }));
        }
    }

    private void addTagFromInput() {
        String text = tagInputField.getText() == null ? "" : tagInputField.getText().trim();
        if (!text.isEmpty()) {
            if (currentEntry != null) {
                currentEntry.addTag(text);
            }
            tagInputField.setText("");
            renderTagsEditor();
        }
    }

    private void renderPhotosEditor() {
        photosFlowPane.getChildren().clear();
        for (String path : currentPhotoPaths) {
            photosFlowPane.getChildren().add(buildPhotoThumbnail(path));
        }
    }

    private StackPane buildPhotoThumbnail(String path) {
        StackPane container = new StackPane();
        container.setPrefSize(110, 110);
        container.setStyle("-fx-background-color: #262626; -fx-background-radius: 10;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(110);
        imageView.setFitHeight(110);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        try {
            File f = new File(path);
            if (f.exists()) {
                imageView.setImage(new Image(f.toURI().toString(), 110, 110, false, true, true));
            }
        } catch (Exception ignored) {}

        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-background-color: rgba(0,0,0,0.65); -fx-text-fill: white; " +
                           "-fx-font-size: 11px; -fx-padding: 2 5; -fx-cursor: hand; " +
                           "-fx-background-radius: 20;");
        StackPane.setAlignment(removeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(removeBtn, new Insets(4));
        removeBtn.setOnAction(e -> {
            currentPhotoPaths.remove(path);
            renderPhotosEditor();
        });

        container.getChildren().addAll(imageView, removeBtn);
        return container;
    }

    // SAVE / DELETE / ADD PHOTO

    private void handleSave() {
        String title   = titleField.getText() == null ? "" : titleField.getText().trim();
        String content = contentArea.getText() == null ? "" : contentArea.getText();
        LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();

        if (title.isEmpty()) {
            title = "Untitled";
            titleField.setText(title);
        }

        String pendingTag = tagInputField.getText() == null ? "" : tagInputField.getText().trim();

        if (currentEntry == null) {
            JournalEntry newEntry = new JournalEntry(title, content, date);
            if (!pendingTag.isEmpty()) newEntry.addTag(pendingTag);
            for (String path : currentPhotoPaths) newEntry.addPhoto(path);
            store.addEntry(newEntry);
            currentEntry = newEntry;
        } else {
            currentEntry.setTitle(title);
            currentEntry.setContent(content);
            currentEntry.setDate(date);
            if (!pendingTag.isEmpty()) currentEntry.addTag(pendingTag);
            currentEntry.getPhotoPaths().clear();
            for (String path : currentPhotoPaths) currentEntry.addPhoto(path);
            store.updateEntry(currentEntry);
        }

        tagInputField.setText("");
        dateTimeText.setText(formatHeaderDateTime(currentEntry));
        deleteButton.setVisible(true);
        deleteButton.setManaged(true);
        renderTagsEditor();
        refreshEntryList();
    }

    private void handleDelete() {
        if (currentEntry == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Entry");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete \"" + currentEntry.getTitle() + "\"? This cannot be undone.");
        styleDialog(confirm.getDialogPane());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                store.deleteEntry(currentEntry.getId());
                currentEntry = null;
                refreshEntryList();
                showEmptyDetail();
            }
        });
    }

    private void handleAddPhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Attach Photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files",
                        "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));

        List<File> files = chooser.showOpenMultipleDialog(addPhotoButton.getScene().getWindow());
        if (files == null || files.isEmpty()) return;

        for (File file : files) {
            Photo imported = store.importPhoto(file.getAbsolutePath(), LocalDate.now());
            if (imported != null) {
                currentPhotoPaths.add(imported.getFilePath());
            }
        }
        renderPhotosEditor();
    }

    // VIEW SWITCHING

    private void showEmptyDetail() {
        detailEmptyState.setVisible(true);
        detailEmptyState.setManaged(true);
        editorScrollPane.setVisible(false);
        editorScrollPane.setManaged(false);
    }

    private void showEditorPane() {
        detailEmptyState.setVisible(false);
        detailEmptyState.setManaged(false);
        editorScrollPane.setVisible(true);
        editorScrollPane.setManaged(true);
    }

    // HELPERS

    // Entries store only their creation instant in their ID, reuse it to show a time-of-day.
    private long createdMillis(JournalEntry entry) {
        try {
            return Long.parseLong(entry.getId());
        } catch (Exception e) {
            return 0L;
        }
    }

    private LocalDateTime createdDateTime(JournalEntry entry) {
        long millis = createdMillis(entry);
        if (millis <= 0L) return entry.getDate() != null ? entry.getDate().atStartOfDay() : LocalDateTime.now();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    private HBox buildTagChip(String tag, Runnable onRemove) {
        HBox chip = new HBox(4);
        chip.setAlignment(Pos.CENTER);
        chip.setStyle("-fx-background-color: #262626; -fx-background-radius: 20; " +
                      "-fx-padding: 3 10 3 10;");

        Label label = new Label(tag);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
        chip.getChildren().add(label);

        if (onRemove != null) {
            Button removeBtn = new Button("✕");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #93a1a1; " +
                               "-fx-padding: 0 0 0 2; -fx-cursor: hand; -fx-font-size: 10px;");
            removeBtn.setOnAction(e -> onRemove.run());
            chip.getChildren().add(removeBtn);
        }
        return chip;
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
