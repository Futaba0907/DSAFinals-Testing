package com.example.dsafinals.controllers;

import com.example.dsafinals.model.AlbumNode;
import com.example.dsafinals.model.JournalEntry;
import com.example.dsafinals.util.SearchUtils;
import com.example.dsafinals.util.SortUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class JournalController implements LibraryAware {
    @FXML
    private ListView<JournalEntry> entryListView;

    @FXML
    private TextField searchField;

    @FXML
    private Label resultLabel;

    private JournalEntry[] sortedByDate = new JournalEntry[0];

    @Override
    public void setLibrary(AlbumNode library) {
        JournalEntry[] entries = library.getAllEntries();
        SortUtils.mergeSort(entries, SortUtils.byDate());
        sortedByDate = entries;
        entryListView.setItems(FXCollections.observableArrayList(entries));
    }

    @FXML
    private void handleSortByDate() {
        JournalEntry[] entries = entryListView.getItems().toArray(new JournalEntry[0]);
        SortUtils.mergeSort(entries, SortUtils.byDate());
        entryListView.setItems(FXCollections.observableArrayList(entries));
    }

    @FXML
    private void handleSortByTitle() {
        JournalEntry[] entries = entryListView.getItems().toArray(new JournalEntry[0]);
        SortUtils.quickSort(entries, SortUtils.byTitle());
        entryListView.setItems(FXCollections.observableArrayList(entries));
    }

    @FXML
    private void handleSearch() {
        String text = searchField.getText();
        if (text == null || text.isBlank()) {
            resultLabel.setText("");
            return;
        }
        try {
            LocalDate target = LocalDate.parse(text.trim());
            JournalEntry found = SearchUtils.binarySearchByDate(sortedByDate, target);
            if (found != null) {
                entryListView.getSelectionModel().select(found);
                entryListView.scrollTo(found);
                resultLabel.setText("Found: " + found.getTitle());
            } else {
                resultLabel.setText("No entry on " + target);
            }
        } catch (DateTimeParseException e) {
            resultLabel.setText("Use date format yyyy-MM-dd");
        }
    }
}
