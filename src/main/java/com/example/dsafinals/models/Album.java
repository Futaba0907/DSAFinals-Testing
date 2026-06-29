package com.example.dsafinals.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an album (folder) of photos.
 * Albums are organized using a Tree structure (see AlbumTree).
 * Each album can have sub-albums (children) and a list of photo IDs.
 */
public class Album {
    private String id;
    private String name;
    private String parentId;          // null if root-level album
    private List<String> photoIds;    // Array: ordered list of photo IDs in this album
    private LocalDate createdDate;

    public Album(String name, String parentId) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.name = name;
        this.parentId = parentId;
        this.photoIds = new ArrayList<>();
        this.createdDate = LocalDate.now();
    }

    public Album(String id, String name, String parentId,
                 List<String> photoIds, LocalDate createdDate) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.photoIds = photoIds != null ? photoIds : new ArrayList<>();
        this.createdDate = createdDate;
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getParentId() { return parentId; }
    public List<String> getPhotoIds() { return photoIds; }
    public LocalDate getCreatedDate() { return createdDate; }

    // --- Setters ---
    public void setName(String name) { this.name = name; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public void addPhoto(String photoId) {
        if (!photoIds.contains(photoId)) photoIds.add(photoId);
    }

    public void removePhoto(String photoId) {
        photoIds.remove(photoId);
    }

    public String serialize() {
        String photosStr = String.join(",", photoIds);
        String safeName = name.replace("|", "\\pipe");
        String safeParent = parentId == null ? "null" : parentId;
        return id + "|" + safeName + "|" + safeParent + "|" + photosStr + "|" + createdDate;
    }

    public static Album deserialize(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 5) return null;

        String id = parts[0];
        String name = parts[1].replace("\\pipe", "|");
        String parentId = parts[2].equals("null") ? null : parts[2];

        List<String> photoIds = new ArrayList<>();
        if (!parts[3].isEmpty()) {
            for (String p : parts[3].split(",")) photoIds.add(p);
        }

        LocalDate createdDate = LocalDate.parse(parts[4]);
        return new Album(id, name, parentId, photoIds, createdDate);
    }

    @Override
    public String toString() {
        return "Album{id='" + id + "', name='" + name + "', photos=" + photoIds.size() + "}";
    }
}
