package com.example.dsafinals.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single photo stored in the local archive.
 */
public class Photo {
    private String id;
    private String filePath;      // Absolute path to the image on disk
    private String fileName;
    private String caption;
    private LocalDate dateTaken;  // Used for memory recall + binary search
    private List<String> tags;

    public Photo(String filePath, String fileName, LocalDate dateTaken) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.filePath = filePath;
        this.fileName = fileName;
        this.caption = "";
        this.dateTaken = dateTaken;
        this.tags = new ArrayList<>();
    }

    public Photo(String id, String filePath, String fileName, String caption,
                 LocalDate dateTaken, List<String> tags) {
        this.id = id;
        this.filePath = filePath;
        this.fileName = fileName;
        this.caption = caption;
        this.dateTaken = dateTaken;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getFilePath() { return filePath; }
    public String getFileName() { return fileName; }
    public String getCaption() { return caption; }
    public LocalDate getDateTaken() { return dateTaken; }
    public List<String> getTags() { return tags; }

    // --- Setters ---
    public void setCaption(String caption) { this.caption = caption; }
    public void setDateTaken(LocalDate date) { this.dateTaken = date; }
    public void addTag(String tag) { if (!tags.contains(tag)) tags.add(tag); }
    public void removeTag(String tag) { tags.remove(tag); }

    public String serialize() {
        String tagsStr = String.join(",", tags);
        String safeCaption = caption.replace("|", "\\pipe");
        return id + "|" + filePath + "|" + fileName + "|" + safeCaption + "|" + dateTaken + "|" + tagsStr;
    }

    public static Photo deserialize(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 6) return null;

        String id = parts[0];
        String filePath = parts[1];
        String fileName = parts[2];
        String caption = parts[3].replace("\\pipe", "|");
        LocalDate dateTaken = LocalDate.parse(parts[4]);

        List<String> tags = new ArrayList<>();
        if (!parts[5].isEmpty()) {
            for (String t : parts[5].split(",")) tags.add(t);
        }

        return new Photo(id, filePath, fileName, caption, dateTaken, tags);
    }

    @Override
    public String toString() {
        return "Photo{id='" + id + "', fileName='" + fileName + "', dateTaken=" + dateTaken + "}";
    }
}
