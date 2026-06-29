package com.example.dsafinals.storage;

import com.example.dsafinals.models.Album;
import com.example.dsafinals.models.JournalEntry;
import com.example.dsafinals.models.Photo;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all file I/O for the application.
 * All data is stored locally in the user's home directory under "DigitalJournal/".
 *
 * Directory structure:
 *   ~/DigitalJournal/
 *     entries.txt       — journal entries
 *     photos.txt        — photo metadata
 *     albums.txt        — album metadata
 *     photos/           — actual copied image files
 */
public class StorageManager {

    // Base directory in user's home folder
    private static final String BASE_DIR = System.getProperty("user.home")
            + File.separator + "DigitalJournal";

    private static final String ENTRIES_FILE = BASE_DIR + File.separator + "entries.txt";
    private static final String PHOTOS_FILE  = BASE_DIR + File.separator + "photos.txt";
    private static final String ALBUMS_FILE  = BASE_DIR + File.separator + "albums.txt";
    public  static final String PHOTOS_DIR   = BASE_DIR + File.separator + "photos";

    // Singleton
    private static StorageManager instance;

    private StorageManager() {
        initDirectories();
    }

    public static StorageManager getInstance() {
        if (instance == null) instance = new StorageManager();
        return instance;
    }

    /** Create com.example.dsafinals.storage directories if they don't exist yet. */
    private void initDirectories() {
        try {
            Files.createDirectories(Paths.get(BASE_DIR));
            Files.createDirectories(Paths.get(PHOTOS_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create com.example.dsafinals.storage directories: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // JOURNAL ENTRIES
    // ─────────────────────────────────────────────

    public void saveEntries(List<JournalEntry> entries) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ENTRIES_FILE))) {
            for (JournalEntry entry : entries) {
                writer.write(entry.serialize());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save entries: " + e.getMessage());
        }
    }

    public List<JournalEntry> loadEntries() {
        List<JournalEntry> entries = new ArrayList<>();
        File file = new File(ENTRIES_FILE);
        if (!file.exists()) return entries;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    JournalEntry entry = JournalEntry.deserialize(line);
                    if (entry != null) entries.add(entry);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load entries: " + e.getMessage());
        }

        return entries;
    }

    // ─────────────────────────────────────────────
    // PHOTOS
    // ─────────────────────────────────────────────

    public void savePhotos(List<Photo> photos) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PHOTOS_FILE))) {
            for (Photo photo : photos) {
                writer.write(photo.serialize());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save photos: " + e.getMessage());
        }
    }

    public List<Photo> loadPhotos() {
        List<Photo> photos = new ArrayList<>();
        File file = new File(PHOTOS_FILE);
        if (!file.exists()) return photos;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Photo photo = Photo.deserialize(line);
                    if (photo != null) photos.add(photo);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load photos: " + e.getMessage());
        }

        return photos;
    }

    /**
     * Copy an image file into the app's managed photos directory.
     * Returns the new internal path, or null on failure.
     */
    public String importPhoto(String sourcePath) {
        try {
            Path source = Paths.get(sourcePath);
            String fileName = source.getFileName().toString();

            // Avoid name collisions by prepending timestamp
            String uniqueName = System.currentTimeMillis() + "_" + fileName;
            Path destination = java.nio.file.Paths.get(PHOTOS_DIR, uniqueName);
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            return destination.toString();
        } catch (IOException e) {
            System.err.println("Failed to import photo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete a photo file from managed com.example.dsafinals.storage.
     */
    public boolean deletePhotoFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            System.err.println("Failed to delete photo file: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────
    // ALBUMS
    // ─────────────────────────────────────────────

    public void saveAlbums(List<Album> albums) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ALBUMS_FILE))) {
            for (Album album : albums) {
                writer.write(album.serialize());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save albums: " + e.getMessage());
        }
    }

    public List<Album> loadAlbums() {
        List<Album> albums = new ArrayList<>();
        File file = new File(ALBUMS_FILE);
        if (!file.exists()) return albums;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Album album = Album.deserialize(line);
                    if (album != null) albums.add(album);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load albums: " + e.getMessage());
        }

        return albums;
    }

    public String getBaseDir() { return BASE_DIR; }
    public String getPhotosDir() { return PHOTOS_DIR; }
}
