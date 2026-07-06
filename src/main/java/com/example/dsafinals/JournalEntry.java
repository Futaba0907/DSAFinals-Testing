package com.example.dsafinals.model;

import java.io.Serializable;
import java.time.LocalDate;

public class JournalEntry implements Serializable {
    private String title;
    private String content;
    private LocalDate date;
    private String[] tags;
    private String[] imagePaths;

    public JournalEntry(String title, String content, LocalDate date, String[] tags, String[] imagePaths) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.tags = tags != null ? tags : new String[0];
        this.imagePaths = imagePaths != null ? imagePaths : new String[0];
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String[] getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(String[] imagePaths) {
        this.imagePaths = imagePaths;
    }

    @Override
    public String toString() {
        return title + " (" + date + ")";
    }
}
