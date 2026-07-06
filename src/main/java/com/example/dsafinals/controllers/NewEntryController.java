package com.example.dsafinals.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewEntryController {
    @FXML
    private TextField titleField;

    @FXML
    private TextArea contentArea;

    @FXML
    private TextField tagsField;

    private boolean saved = false;

    @FXML
    private void handleSave() {
        saved = true;
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        saved = false;
        closeDialog();
    }

    private void closeDialog() {
        ((Stage) titleField.getScene().getWindow()).close();
    }

    public boolean isSaved() {
        return saved && titleField.getText() != null && !titleField.getText().isBlank();
    }

    public String getTitle() {
        return titleField.getText();
    }

    public String getContent() {
        return contentArea.getText();
    }

    public String[] getTags() {
        String text = tagsField.getText();
        if (text == null || text.isBlank()) return new String[0];
        String[] parts = text.split(",");
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        return parts;
    }
}
