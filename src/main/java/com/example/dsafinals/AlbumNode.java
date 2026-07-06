package com.example.dsafinals.model;

import com.example.dsafinals.datastructures.DynamicArray;

import java.io.Serializable;

public class AlbumNode implements Serializable {
    private String name;
    private AlbumNode parent;
    private final DynamicArray<AlbumNode> children = new DynamicArray<>();
    private final DynamicArray<JournalEntry> entries = new DynamicArray<>();

    public AlbumNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlbumNode getParent() {
        return parent;
    }

    public AlbumNode addChild(String childName) {
        AlbumNode child = new AlbumNode(childName);
        child.parent = this;
        children.add(child);
        return child;
    }

    public boolean removeChild(AlbumNode child) {
        return children.remove(child);
    }

    public DynamicArray<AlbumNode> getChildren() {
        return children;
    }

    public void addEntry(JournalEntry entry) {
        entries.add(entry);
    }

    public boolean removeEntry(JournalEntry entry) {
        return entries.remove(entry);
    }

    public DynamicArray<JournalEntry> getEntries() {
        return entries;
    }

    public AlbumNode findNode(String targetName) {
        if (name.equals(targetName)) return this;
        for (AlbumNode child : children) {
            AlbumNode found = child.findNode(targetName);
            if (found != null) return found;
        }
        return null;
    }

    public JournalEntry[] getAllEntries() {
        DynamicArray<JournalEntry> collected = new DynamicArray<>();
        collectEntries(this, collected);
        return collected.toArray(new JournalEntry[0]);
    }

    private static void collectEntries(AlbumNode node, DynamicArray<JournalEntry> collector) {
        for (JournalEntry entry : node.entries) collector.add(entry);
        for (AlbumNode child : node.children) collectEntries(child, collector);
    }

    public String getPath() {
        if (parent == null) return name;
        return parent.getPath() + " / " + name;
    }

    @Override
    public String toString() {
        return name;
    }
}
