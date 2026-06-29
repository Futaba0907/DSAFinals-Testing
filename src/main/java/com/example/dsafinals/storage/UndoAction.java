package com.example.dsafinals.storage;

/**
 * Represents a reversible user action stored in the undo/redo stacks.
 */
public class UndoAction {
    public enum Type {
        ADD_ENTRY,
        DELETE_ENTRY,
        EDIT_ENTRY,
        ADD_PHOTO,
        DELETE_PHOTO
    }

    public final Type type;
    public final Object data; // JournalEntry or Photo snapshot

    public UndoAction(Type type, Object data) {
        this.type = type;
        this.data = data;
    }
}
