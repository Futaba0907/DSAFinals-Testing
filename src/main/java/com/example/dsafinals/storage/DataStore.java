package com.example.dsafinals.storage;

import com.example.dsafinals.datastructures.*;
import com.example.dsafinals.models.Album;
import com.example.dsafinals.models.JournalEntry;
import com.example.dsafinals.models.Photo;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Central data store for the application.
 * Holds all in-memory data and coordinates between storage and data structures.
 *
 * Use DataStore.getInstance() everywhere in the app.
 */
public class DataStore {

    // ── In-memory data (arrays / lists) ──────────────────────────
    private List<JournalEntry> entries;
    private List<Photo> photos;

    // ── Data structures ───────────────────────────────────────────
    private AlbumTree albumTree;
    private AppStack<UndoAction> undoStack;
    private AppStack<UndoAction> redoStack;
    private AppQueue<Runnable> taskQueue;

    // ── File storage ──────────────────────────────────────────────
    private final StorageManager storage;

    // ── Listeners (notified whenever the undo/redo stacks change) ──
    private final List<Runnable> stackChangeListeners = new ArrayList<>();

    // ── Singleton ─────────────────────────────────────────────────
    private static DataStore instance;

    private DataStore() {
        storage = StorageManager.getInstance();
        undoStack = new AppStack<>();
        redoStack = new AppStack<>();
        taskQueue = new AppQueue<>();
        albumTree = new AlbumTree();
        load();
    }

    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    public void load() {
        entries = storage.loadEntries();
        photos = storage.loadPhotos();
        List<Album> albums = storage.loadAlbums();
        albumTree.buildFromList(albums);
    }

    public void saveAll() {
        storage.saveEntries(entries);
        storage.savePhotos(photos);
        storage.saveAlbums(albumTree.toFlatList());
    }

    // ─────────────────────────────────────────────
    // JOURNAL ENTRIES
    // ─────────────────────────────────────────────

    public List<JournalEntry> getEntries() { return entries; }

    public void addEntry(JournalEntry entry) {
        entries.add(entry);
        storage.saveEntries(entries);
        pushUndo(new UndoAction(UndoAction.Type.ADD_ENTRY, entry));
    }

    public void updateEntry(JournalEntry updated) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId().equals(updated.getId())) {
                pushUndo(new UndoAction(UndoAction.Type.EDIT_ENTRY, entries.get(i)));
                entries.set(i, updated);
                break;
            }
        }
        storage.saveEntries(entries);
    }

    public void deleteEntry(String entryId) {
        entries.removeIf(e -> {
            if (e.getId().equals(entryId)) {
                pushUndo(new UndoAction(UndoAction.Type.DELETE_ENTRY, e));
                return true;
            }
            return false;
        });
        storage.saveEntries(entries);
    }

    public JournalEntry findEntryById(String id) {
        for (JournalEntry e : entries) {
            if (e.getId().equals(id)) return e;
        }
        return null;
    }

    public List<JournalEntry> getEntriesSortedByDate() {
        return Sorter.mergeSortByDate(new ArrayList<>(entries));
    }

    public List<JournalEntry> getEntriesSortedByTitle() {
        return Sorter.mergeSortByTitle(new ArrayList<>(entries));
    }

    public List<JournalEntry> searchEntriesByDate(LocalDate date) {
        return BinarySearcher.searchEntriesByDate(getEntriesSortedByDate(), date);
    }

    public List<JournalEntry> getEntriesInRange(LocalDate start, LocalDate end) {
        return BinarySearcher.searchEntriesInRange(getEntriesSortedByDate(), start, end);
    }

    public List<JournalEntry> searchEntriesByKeyword(String keyword) {
        String lower = keyword.toLowerCase();
        return entries.stream()
                .filter(e -> e.getTitle().toLowerCase().contains(lower)
                        || e.getContent().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<JournalEntry> searchEntriesByTag(String tag) {
        return entries.stream()
                .filter(e -> e.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // PHOTOS
    // ─────────────────────────────────────────────

    public List<Photo> getPhotos() { return photos; }

    public void addPhoto(Photo photo) {
        photos.add(photo);
        storage.savePhotos(photos);
        pushUndo(new UndoAction(UndoAction.Type.ADD_PHOTO, photo));
    }

    public void deletePhoto(String photoId) {
        photos.removeIf(p -> {
            if (p.getId().equals(photoId)) {
                pushUndo(new UndoAction(UndoAction.Type.DELETE_PHOTO, p));
                storage.deletePhotoFile(p.getFilePath());
                return true;
            }
            return false;
        });
        storage.savePhotos(photos);
    }

    public Photo findPhotoById(String id) {
        for (Photo p : photos) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    public List<Photo> getPhotosSortedByDate() {
        List<Photo> sorted = new ArrayList<>(photos);
        if (!sorted.isEmpty()) Sorter.quickSortPhotosByDate(sorted, 0, sorted.size() - 1);
        return sorted;
    }

    public List<Photo> getPhotosInRange(LocalDate start, LocalDate end) {
        return BinarySearcher.searchPhotosInRange(getPhotosSortedByDate(), start, end);
    }

    public Photo importPhoto(String sourcePath, LocalDate dateTaken) {
        String newPath = storage.importPhoto(sourcePath);
        if (newPath == null) return null;

        String fileName = Paths.get(newPath).getFileName().toString();
        Photo photo = new Photo(newPath, fileName, dateTaken);
        addPhoto(photo);
        return photo;
    }

    // ─────────────────────────────────────────────
    // ALBUMS
    // ─────────────────────────────────────────────

    public AlbumTree getAlbumTree() { return albumTree; }

    public void addAlbum(Album album) {
        albumTree.addAlbum(album);
        storage.saveAlbums(albumTree.toFlatList());
    }

    public void deleteAlbum(String albumId) {
        albumTree.removeAlbum(albumId);
        storage.saveAlbums(albumTree.toFlatList());
    }

    // ─────────────────────────────────────────────
    // UNDO / REDO
    // ─────────────────────────────────────────────

    /**
     * Register a callback to be run whenever the undo/redo stacks change
     * (an action is recorded, undone, or redone). Lets the UI stay in sync
     * with stack state without needing to poll or reload on navigation.
     */
    public void addStackChangeListener(Runnable listener) {
        stackChangeListeners.add(listener);
    }

    private void notifyStackChanged() {
        for (Runnable listener : new ArrayList<>(stackChangeListeners)) {
            listener.run();
        }
    }

    private void pushUndo(UndoAction action) {
        undoStack.push(action);
        redoStack.clear();
        notifyStackChanged();
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    public void undo() {
        if (!canUndo()) return;
        UndoAction action = undoStack.pop();
        applyUndo(action);
        redoStack.push(action);
        saveAll();
        notifyStackChanged();
    }

    public void redo() {
        if (!canRedo()) return;
        UndoAction action = redoStack.pop();
        applyRedo(action);
        undoStack.push(action);
        saveAll();
        notifyStackChanged();
    }

    private void applyUndo(UndoAction action) {
        switch (action.type) {
            case ADD_ENTRY    -> entries.removeIf(e -> e.getId().equals(((JournalEntry) action.data).getId()));
            case DELETE_ENTRY -> entries.add((JournalEntry) action.data);
            case EDIT_ENTRY   -> updateEntryDirect((JournalEntry) action.data);
            case ADD_PHOTO    -> photos.removeIf(p -> p.getId().equals(((Photo) action.data).getId()));
            case DELETE_PHOTO -> photos.add((Photo) action.data);
        }
    }

    private void applyRedo(UndoAction action) {
        switch (action.type) {
            case ADD_ENTRY    -> entries.add((JournalEntry) action.data);
            case DELETE_ENTRY -> entries.removeIf(e -> e.getId().equals(((JournalEntry) action.data).getId()));
            case EDIT_ENTRY   -> updateEntryDirect((JournalEntry) action.data);
            case ADD_PHOTO    -> photos.add((Photo) action.data);
            case DELETE_PHOTO -> photos.removeIf(p -> p.getId().equals(((Photo) action.data).getId()));
        }
    }

    private void updateEntryDirect(JournalEntry entry) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId().equals(entry.getId())) {
                entries.set(i, entry);
                return;
            }
        }
    }

    // ─────────────────────────────────────────────
    // MEMORY RECALL
    // ─────────────────────────────────────────────

    public List<JournalEntry> getOnThisDayEntries() {
        LocalDate today = LocalDate.now();
        AppQueue<JournalEntry> queue = new AppQueue<>();

        for (JournalEntry entry : getEntriesSortedByDate()) {
            LocalDate d = entry.getDate();
            if (d.getMonthValue() == today.getMonthValue()
                    && d.getDayOfMonth() == today.getDayOfMonth()
                    && d.getYear() < today.getYear()) {
                queue.enqueue(entry);
            }
        }

        List<JournalEntry> result = new ArrayList<>();
        while (!queue.isEmpty()) result.add(queue.dequeue());
        return result;
    }

    public List<Photo> getOnThisDayPhotos() {
        LocalDate today = LocalDate.now();
        AppQueue<Photo> queue = new AppQueue<>();

        for (Photo photo : getPhotosSortedByDate()) {
            LocalDate d = photo.getDateTaken();
            if (d.getMonthValue() == today.getMonthValue()
                    && d.getDayOfMonth() == today.getDayOfMonth()
                    && d.getYear() < today.getYear()) {
                queue.enqueue(photo);
            }
        }

        List<Photo> result = new ArrayList<>();
        while (!queue.isEmpty()) result.add(queue.dequeue());
        return result;
    }

    // ─────────────────────────────────────────────
    // TASK QUEUE
    // ─────────────────────────────────────────────

    public void enqueueTask(Runnable task) { taskQueue.enqueue(task); }

    public void processNextTask() {
        if (!taskQueue.isEmpty()) taskQueue.dequeue().run();
    }
}
