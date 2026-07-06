package com.example.dsafinals.io;

import com.example.dsafinals.model.AlbumNode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileManager {
    private static final Path DATA_DIR = Paths.get(System.getProperty("user.home"), ".digitaljournal");
    private static final Path LIBRARY_FILE = DATA_DIR.resolve("library.dat");

    public static void saveLibrary(AlbumNode root) throws IOException {
        Files.createDirectories(DATA_DIR);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(LIBRARY_FILE.toFile()))) {
            out.writeObject(root);
        }
    }

    public static AlbumNode loadLibrary() throws IOException, ClassNotFoundException {
        if (!Files.exists(LIBRARY_FILE)) return null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(LIBRARY_FILE.toFile()))) {
            return (AlbumNode) in.readObject();
        }
    }

    public static Path saveImage(File sourceFile, String albumName) throws IOException {
        Path albumDir = DATA_DIR.resolve("images").resolve(albumName);
        Files.createDirectories(albumDir);
        Path destination = albumDir.resolve(sourceFile.getName());
        Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination;
    }

    public static Path getDataDirectory() {
        return DATA_DIR;
    }
}
