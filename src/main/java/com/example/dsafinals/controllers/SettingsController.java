package com.example.dsafinals.controllers;

import com.example.dsafinals.models.JournalEntry;
import com.example.dsafinals.storage.DataStore;
import com.example.dsafinals.storage.StorageManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.DialogPane;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SettingsController {

    @FXML private Label storagePathLabel;
    @FXML private Label entryCountLabel;
    @FXML private Label photoCountLabel;
    @FXML private Button exportButton;
    @FXML private Button clearDataButton;

    private final DataStore store = DataStore.getInstance();

    @FXML
    public void initialize() {
        // Storage path
        storagePathLabel.setText(StorageManager.getInstance().getBaseDir());

        // Counts
        refreshCounts();

        exportButton.setOnAction(e -> handleExport());
        clearDataButton.setOnAction(e -> handleClearData());
    }

    private void refreshCounts() {
        entryCountLabel.setText(store.getEntries().size() + " journal entries");
        photoCountLabel.setText(store.getPhotos().size() + " photos");
    }

    // EXPORT

    private void handleExport() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Export Folder");
        File dir = chooser.showDialog(exportButton.getScene().getWindow());
        if (dir == null) return;

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "journal_export_" + timestamp + ".txt";
        File outFile = new File(dir, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writer.write("═══════════════════════════════════════════════");
            writer.newLine();
            writer.write("  LOCAL PHOTO ARCHIVE & DIGITAL JOURNAL");
            writer.newLine();
            writer.write("  Exported: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("MMMM d, yyyy  HH:mm")));
            writer.newLine();
            writer.write("═══════════════════════════════════════════════");
            writer.newLine();
            writer.newLine();

            List<JournalEntry> entries = store.getEntriesSortedByDate();
            if (entries.isEmpty()) {
                writer.write("No journal entries found.");
                writer.newLine();
            } else {
                for (JournalEntry entry : entries) {
                    writer.write("───────────────────────────────────────────────");
                    writer.newLine();
                    writer.write("Title  : " + entry.getTitle());
                    writer.newLine();
                    writer.write("Date   : " + entry.getDate());
                    writer.newLine();
                    if (!entry.getTags().isEmpty()) {
                        writer.write("Tags   : " + String.join(", ", entry.getTags()));
                        writer.newLine();
                    }
                    if (!entry.getPhotoPaths().isEmpty()) {
                        writer.write("Photos : " + entry.getPhotoPaths().size() + " attached");
                        writer.newLine();
                    }
                    writer.newLine();
                    writer.write(entry.getContent() == null ? "" : entry.getContent());
                    writer.newLine();
                    writer.newLine();
                }
            }

            writer.write("═══════════════════════════════════════════════");
            writer.newLine();
            writer.write("  Total entries : " + entries.size());
            writer.newLine();
            writer.write("  Total photos  : " + store.getPhotos().size());
            writer.newLine();
            writer.write("═══════════════════════════════════════════════");

            showInfo("Export Successful",
                    "Entries exported to:\n" + outFile.getAbsolutePath());

        } catch (IOException e) {
            showError("Export Failed", e.getMessage());
        }
    }

    // CLEAR ALL DATA

    private void handleClearData() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear All Data");
        confirm.setHeaderText("This will permanently delete ALL entries, photos, and albums.");
        confirm.setContentText("This action cannot be undone. Are you sure?");
        styleDialog(confirm.getDialogPane());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Second confirmation
                Alert second = new Alert(Alert.AlertType.CONFIRMATION);
                second.setTitle("Are you absolutely sure?");
                second.setHeaderText(null);
                second.setContentText("Type OK to confirm permanent deletion of all data.");
                styleDialog(second.getDialogPane());

                second.showAndWait().ifPresent(r2 -> {
                    if (r2 == ButtonType.OK) {
                        performClear();
                    }
                });
            }
        });
    }

    private void performClear() {
        // Clear in-memory lists by deleting each entry/photo individually
        // so undo stack stays consistent
        List<JournalEntry> entries = new java.util.ArrayList<>(store.getEntries());
        for (JournalEntry e : entries) store.deleteEntry(e.getId());

        List<com.example.dsafinals.models.Photo> photos =
                new java.util.ArrayList<>(store.getPhotos());
        for (com.example.dsafinals.models.Photo p : photos) store.deletePhoto(p.getId());

        List<com.example.dsafinals.models.Album> albums =
                store.getAlbumTree().toFlatList();
        for (com.example.dsafinals.models.Album a : albums) store.deleteAlbum(a.getId());

        store.saveAll();
        refreshCounts();
        showInfo("Data Cleared", "All entries, photos, and albums have been deleted.");
    }

    // Functions Pop-up (For changes)

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert.getDialogPane());
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert.getDialogPane());
        alert.showAndWait();
    }

    private void styleDialog(DialogPane pane) {
        pane.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;");
        if (pane.lookupButton(ButtonType.OK) != null)
            pane.lookupButton(ButtonType.OK).setStyle(
                    "-fx-background-color: #155dfc; -fx-text-fill: white; -fx-font-weight: bold;");
        if (pane.lookupButton(ButtonType.CANCEL) != null)
            pane.lookupButton(ButtonType.CANCEL).setStyle(
                    "-fx-background-color: #262626; -fx-text-fill: white;");
    }
}
