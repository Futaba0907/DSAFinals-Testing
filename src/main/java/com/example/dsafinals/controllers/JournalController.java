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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JournalController {

    // ── List View ─────────────────────────────────
    @FXML private ScrollPane listScrollPane;
    @FXML private VBox       entryListContainer;
    @FXML private StackPane  emptyState;
    @FXML private Button     newEntryButton;
    @FXML private ComboBox<String> sortComboBox;

    // ── Editor View ───────────────────────────────
    @FXML private ScrollPane editorScrollPane;
    @FXML private Button     backButton;
    @FXML private Button     saveButton;
    @FXML private Button     deleteButton;
    @FXML private TextField  titleField;
    @FXML private DatePicker datePicker;
    @FXML private TextField  tagInputField;
    @FXML private FlowPane   tagsFlowPane;
    @FXML private TextArea   contentArea;
    @FXML private FlowPane   photosFlowPane;
    @FXML private Button     addPhotoButton;

    private final DataStore store = DataStore.getInstance();
    private JournalEntry currentEntry = null;   // null → creating new
    private final List<String> currentPhotoPaths = new ArrayList<>();

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy");

    // ─────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────

    @FXML
    public void initialize() {
        sortComboBox.getItems().addAll(
                "Date (Newest First)", "Date (Oldest First)", "Title (A-Z)");
        sortComboBox.setValue("Date (Newest First)");
        sortComboBox.setOnAction(e -> refreshEntryList());

        newEntryButton.setOnAction(e -> openEditor(null));
        backButton.setOnAction(e -> showListView());
        saveButton.setOnAction(e -> handleSave());
        deleteButton.setOnAction(e -> handleDelete());
        addPhotoButton.setOnAction(e -> handleAddPhoto());
        searchField.setOnKeyReleased(e -> handleSearch());

        tagInputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) addTagFromInput();
        });

        refreshEntryList();
        showListView();
    }

    // ─────────────────────────────────────────────
    // ENTRY LIST
    // ─────────────────────────────────────────────

    private void refreshEntryList() {
        entryListContainer.getChildren().clear();

        List<JournalEntry> sorted = getSortedEntries();
        boolean isEmpty = sorted.isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);

        for (JournalEntry entry : sorted) {
            entryListContainer.getChildren().add(buildEntryCard(entry));
        }
    }

    private List<JournalEntry> getSortedEntries() {
        String sort = sortComboBox.getValue();
        if (sort == null) return store.getEntriesSortedByDate();

        return switch (sort) {
            case "Date (Newest First)" -> {
                List<JournalEntry> list = store.getEntriesSortedByDate();
                java.util.Collections.reverse(list);
                yield list;
            }
            case "Date (Oldest First)" -> store.getEntriesSortedByDate();
            case "Title (A-Z)"         -> store.getEntriesSortedByTitle();
            default                    -> store.getEntriesSortedByDate();
        };
    }

    private HBox buildEntryCard(JournalEntry entry) {
        HBox card = new HBox(16);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 12; -fx-cursor: hand;");

        // Hover effect
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 12; -fx-cursor: hand;"));
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 12; -fx-cursor: hand;"));
        card.setOnMouseClicked(e -> openEditor(entry));

        // Text column
        VBox textCol = new VBox(6);
        HBox.setHgrow(textCol, Priority.ALWAYS);

        String displayTitle = (entry.getTitle() == null || entry.getTitle().isBlank())
                ? "Untitled" : entry.getTitle();

        Label title = new Label(displayTitle);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label date = new Label(entry.getDate() != null ? entry.getDate().format(DATE_FMT) : "");
        date.setStyle("-fx-text-fill: #93a1a1; -fx-font-size: 12px;");

        String snippetText = entry.getContent() == null ? "" :
                entry.getContent().replaceAll("\\s+", " ").trim();
        if (snippetText.length() > 140) snippetText = snippetText.substring(0, 140) + "…";
        Label snippet = new Label(snippetText);
        snippet.setStyle("-fx-text-fill: #93a1a1; -fx-font-size: 13px;");
        snippet.setWrapText(true);
        snippet.setMaxWidth(560);

        // Tag chips
        FlowPane tagsRow = new FlowPane(6, 4);
        for (String tag : entry.getTags()) {
            tagsRow.getChildren().add(buildTagChip(tag, null));
        }

        textCol.getChildren().addAll(title, date, snippet, tagsRow);

        // Meta column (photo count)
        VBox metaCol = new VBox();
        metaCol.setAlignment(Pos.TOP_RIGHT);
        int photoCount = entry.getPhotoPaths().size();
        if (photoCount > 0) {
            Label photoLbl = new Label("📷 " + photoCount);
            photoLbl.setStyle("-fx-text-fill: #93a1a1; -fx-font-size: 12px;");
            metaCol.getChildren().add(photoLbl);
        }

        card.getChildren().addAll(textCol, metaCol);
        return card;
    }

    // ─────────────────────────────────────────────
    // EDITOR
    // ─────────────────────────────────────────────

    public void openEditor(JournalEntry entry) {
        currentEntry = entry;
        currentPhotoPaths.clear();

        if (entry != null) {
            titleField.setText(entry.getTitle());
            datePicker.setValue(entry.getDate() != null ? entry.getDate() : LocalDate.now());
            contentArea.setText(entry.getContent());
            currentPhotoPaths.addAll(entry.getPhotoPaths());
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
        } else {
            titleField.setText("");
            datePicker.setValue(LocalDate.now());
            contentArea.setText("");
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }

        tagInputField.setText("");
        renderTagsEditor();
        renderPhotosEditor();
        showEditorView();
        titleField.requestFocus();
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
            } else {
                // new entry not yet created — stash in a temp list until save
                // handled via tempTags below
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

    // ─────────────────────────────────────────────
    // SAVE / DELETE / ADD PHOTO
    // ─────────────────────────────────────────────

    private void handleSave() {
        String title   = titleField.getText() == null ? "" : titleField.getText().trim();
        String content = contentArea.getText() == null ? "" : contentArea.getText();
        LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();

        if (title.isEmpty()) {
            title = "Untitled";
            titleField.setText(title);
        }

        if (currentEntry == null) {
            // New entry
            JournalEntry newEntry = new JournalEntry(title, content, date);
            // Flush any tags typed in the input that haven't been "entered" yet
            String pendingTag = tagInputField.getText() == null ? "" : tagInputField.getText().trim();
            if (!pendingTag.isEmpty()) newEntry.addTag(pendingTag);
            for (String path : currentPhotoPaths) newEntry.addPhoto(path);
            store.addEntry(newEntry);
        } else {
            // Update existing
            currentEntry.setTitle(title);
            currentEntry.setContent(content);
            currentEntry.setDate(date);
            // Flush any pending tag
            String pendingTag = tagInputField.getText() == null ? "" : tagInputField.getText().trim();
            if (!pendingTag.isEmpty()) currentEntry.addTag(pendingTag);
            // Sync photo paths
            currentEntry.getPhotoPaths().clear();
            for (String path : currentPhotoPaths) currentEntry.addPhoto(path);
            store.updateEntry(currentEntry);
        }

        refreshEntryList();
        showListView();
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
                refreshEntryList();
                showListView();
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
            // Import into managed storage (copies the file, registers it in DataStore)
            Photo imported = store.importPhoto(file.getAbsolutePath(), LocalDate.now());
            if (imported != null) {
                currentPhotoPaths.add(imported.getFilePath());
            }
        }
        renderPhotosEditor();
    }

    // ─────────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────────

    // Called from FXML onKeyReleased if you want live search (optional hookup)
    @FXML
    private TextField searchField;

    private void handleSearch() {
        String keyword = searchField == null ? "" :
                (searchField.getText() == null ? "" : searchField.getText().trim());
        entryListContainer.getChildren().clear();

        List<JournalEntry> results = keyword.isEmpty()
                ? getSortedEntries()
                : store.searchEntriesByKeyword(keyword);

        emptyState.setVisible(results.isEmpty());
        emptyState.setManaged(results.isEmpty());

        for (JournalEntry e : results) {
            entryListContainer.getChildren().add(buildEntryCard(e));
        }
    }

    // ─────────────────────────────────────────────
    // VIEW SWITCHING
    // ─────────────────────────────────────────────

    private void showListView() {
        currentEntry = null;
        listScrollPane.setVisible(true);
        listScrollPane.setManaged(true);
        editorScrollPane.setVisible(false);
        editorScrollPane.setManaged(false);
    }

    private void showEditorView() {
        listScrollPane.setVisible(false);
        listScrollPane.setManaged(false);
        editorScrollPane.setVisible(true);
        editorScrollPane.setManaged(true);
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    /** Tag chip. onRemove may be null (read-only chip on entry cards). */
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
