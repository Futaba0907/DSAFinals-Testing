package com.example.dsafinals.datastructures;

import com.example.dsafinals.models.Album;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree structure for organizing Albums hierarchically.
 * Each node holds an Album and can have child albums (sub-folders).
 *
 * DSA Concept: Tree (hierarchical data organization)
 */
public class AlbumTree {

    /** A single node in the album tree. */
    public static class AlbumNode {
        public Album album;
        public List<AlbumNode> children;

        public AlbumNode(Album album) {
            this.album = album;
            this.children = new ArrayList<>();
        }

        public void addChild(AlbumNode child) {
            children.add(child);
        }

        public boolean removeChild(String albumId) {
            return children.removeIf(c -> c.album.getId().equals(albumId));
        }

        public AlbumNode findChild(String albumId) {
            for (AlbumNode child : children) {
                if (child.album.getId().equals(albumId)) return child;
            }
            return null;
        }
    }

    private AlbumNode root; // Virtual root (not an actual album, just a container)

    public AlbumTree() {
        // Virtual root node with a placeholder album
        this.root = new AlbumNode(new Album("ROOT", null));
    }

    /**
     * Build the tree from a flat list of albums (loaded from file).
     * Albums with parentId == null go under root.
     */
    public void buildFromList(List<Album> albums) {
        root.children.clear();

        // First pass: create all nodes
        List<AlbumNode> nodes = new ArrayList<>();
        for (Album a : albums) {
            nodes.add(new AlbumNode(a));
        }

        // Second pass: wire parent-child relationships
        for (AlbumNode node : nodes) {
            String parentId = node.album.getParentId();
            if (parentId == null) {
                root.addChild(node);
            } else {
                AlbumNode parent = findNode(root, parentId);
                if (parent != null) {
                    parent.addChild(node);
                } else {
                    root.addChild(node); // Orphaned album goes to root
                }
            }
        }
    }

    /** Add a new album under a parent (or root if parentId is null). */
    public void addAlbum(Album album) {
        AlbumNode newNode = new AlbumNode(album);
        String parentId = album.getParentId();
        if (parentId == null) {
            root.addChild(newNode);
        } else {
            AlbumNode parent = findNode(root, parentId);
            if (parent != null) {
                parent.addChild(newNode);
            } else {
                root.addChild(newNode);
            }
        }
    }

    /** Remove an album by ID (also removes all children). */
    public boolean removeAlbum(String albumId) {
        return removeFromNode(root, albumId);
    }

    private boolean removeFromNode(AlbumNode node, String albumId) {
        if (node.removeChild(albumId)) return true;
        for (AlbumNode child : node.children) {
            if (removeFromNode(child, albumId)) return true;
        }
        return false;
    }

    /** Find a node anywhere in the tree by album ID. */
    public AlbumNode findNode(String albumId) {
        return findNode(root, albumId);
    }

    private AlbumNode findNode(AlbumNode node, String albumId) {
        if (node.album.getId().equals(albumId)) return node;
        for (AlbumNode child : node.children) {
            AlbumNode found = findNode(child, albumId);
            if (found != null) return found;
        }
        return null;
    }

    /** Get all root-level albums (direct children of virtual root). */
    public List<AlbumNode> getRootAlbums() {
        return root.children;
    }

    /**
     * Flatten the tree back into a list (for saving to file).
     * Uses depth-first traversal.
     */
    public List<Album> toFlatList() {
        List<Album> result = new ArrayList<>();
        for (AlbumNode child : root.children) {
            collectAlbums(child, result);
        }
        return result;
    }

    private void collectAlbums(AlbumNode node, List<Album> result) {
        result.add(node.album);
        for (AlbumNode child : node.children) {
            collectAlbums(child, result);
        }
    }

    /** Print tree to console for debugging. */
    public void printTree() {
        System.out.println("Albums:");
        for (AlbumNode child : root.children) {
            printNode(child, "  ");
        }
    }

    private void printNode(AlbumNode node, String indent) {
        System.out.println(indent + "📁 " + node.album.getName()
                + " (" + node.album.getPhotoIds().size() + " photos)");
        for (AlbumNode child : node.children) {
            printNode(child, indent + "  ");
        }
    }
}
