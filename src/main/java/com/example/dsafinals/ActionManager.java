package com.example.dsafinals.action;

import com.example.dsafinals.datastructures.LinkedStack;

public class ActionManager {
    private final LinkedStack<Action> undoStack = new LinkedStack<>();
    private final LinkedStack<Action> redoStack = new LinkedStack<>();

    public void perform(Action action) {
        action.redo();
        undoStack.push(action);
        redoStack.clear();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        Action action = undoStack.pop();
        action.undo();
        redoStack.push(action);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        Action action = redoStack.pop();
        action.redo();
        undoStack.push(action);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
