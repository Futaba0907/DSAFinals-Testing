package com.example.dsafinals.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single journal entry.
 * Uses an array (ArrayList) internally for tags and attached photo paths.
 */
public class JournalEntry {
    private String id;           // Unique ID (timestamp-based)
    private String title;
    private String content;
    private LocalDate date;
    private List<String> tags;          // Array concept: ordered list of tags
    private List<String> photoPaths;    // Array concept: ordered list of attached photo file paths

    public JournalEntry(String title, String content, LocalDate date) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.title = title;
        this.content = content;
        this.date = date;
        this.tags = new ArrayList<>();
        this.photoPaths = new ArrayList<>();
    }

    // Used when loading from file (ID already exists)
    public JournalEntry(String id, String title, String content, LocalDate date,
                        List<String> tags, List<String> photoPaths) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.photoPaths = photoPaths != null ? photoPaths : new ArrayList<>();
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDate getDate() { return date; }
    public List<String> getTags() { return tags; }
    public List<String> getPhotoPaths() { return photoPaths; }

    // --- Setters ---
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setDate(LocalDate date) { this.date = date; }

    public void addTag(String tag) {
        if (!tags.contains(tag)) tags.add(tag);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void addPhoto(String path) {
        if (!photoPaths.contains(path)) photoPaths.add(path);
    }

    public void removePhoto(String path) {
        photoPaths.remove(path);
    }

    /**
     * Serialize to a simple pipe-delimited string for file com.example.dsafinals.storage.
     * Format: id|title|content|date|tag1,tag2|photo1,photo2
     */
    public String serialize() {
        String tagsStr = String.join(",", tags);
        String photosStr = String.join(",", photoPaths);
        // Escape newlines in content so each entry stays on one line
        String safeContent = content.replace("\n", "\\n").replace("|", "\\pipe");
        String safeTitle = title.replace("|", "\\pipe");
        return id + "|" + safeTitle + "|" + safeContent + "|" + date + "|" + tagsStr + "|" + photosStr;
    }

    /**
     * Deserialize from a pipe-delimited string.
     */
    public static JournalEntry deserialize(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 6) return null;

        String id = parts[0];
        String title = parts[1].replace("\\pipe", "|");
        String content = parts[2].replace("\\n", "\n").replace("\\pipe", "|");
        LocalDate date = LocalDate.parse(parts[3]);

        List<String> tags = new ArrayList<>();
        if (!parts[4].isEmpty()) {
            for (String t : parts[4].split(",")) tags.add(t);
        }

        List<String> photos = new ArrayList<>();
        if (!parts[5].isEmpty()) {
            for (String p : parts[5].split(",")) photos.add(p);
        }

        return new JournalEntry(id, title, content, date, tags, photos);
    }

    @Override
    public String toString() {
        return "JournalEntry{id='" + id + "', title='" + title + "', date=" + date + "}";
    }
}
