package com.example.dsafinals.controllers;

import com.example.dsafinals.model.AlbumNode;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class AlbumsController implements LibraryAware {
    @FXML
    private TreeView<AlbumNode> albumTree;

    @Override
    public void setLibrary(AlbumNode library) {
        TreeItem<AlbumNode> root = buildTree(library);
        albumTree.setRoot(root);
        albumTree.setShowRoot(true);
    }

    private TreeItem<AlbumNode> buildTree(AlbumNode node) {
        TreeItem<AlbumNode> item = new TreeItem<>(node);
        item.setExpanded(true);
        for (AlbumNode child : node.getChildren()) {
            item.getChildren().add(buildTree(child));
        }
        return item;
    }
}
