package com.example.dsafinals;

import com.example.dsafinals.action.ActionManager;
import com.example.dsafinals.model.AlbumNode;
import com.example.dsafinals.model.JournalEntry;
import com.example.dsafinals.service.ImageLoadQueue;
import com.example.dsafinals.util.SearchUtils;
import com.example.dsafinals.util.SortUtils;

import java.time.LocalDate;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        AlbumNode root = new AlbumNode("Library");
        AlbumNode trips = root.addChild("Trips");
        AlbumNode japan = trips.addChild("Japan");

        japan.addEntry(new JournalEntry("Osaka Day 1", "Arrived and explored Dotonbori.",
                LocalDate.of(2026, 3, 12), new String[]{"food", "travel"}, new String[]{"osaka1.jpg"}));
        japan.addEntry(new JournalEntry("Kyoto Temples", "Visited Fushimi Inari.",
                LocalDate.of(2026, 3, 10), new String[]{"travel"}, new String[]{"kyoto1.jpg"}));
        root.addEntry(new JournalEntry("Random Thought", "Nothing planned today.",
                LocalDate.of(2026, 3, 11), new String[0], new String[0]));

        JournalEntry[] all = root.getAllEntries();
        System.out.println("Found node: " + root.findNode("Japan"));

        SortUtils.mergeSort(all, SortUtils.byDate());
        for (JournalEntry e : all) System.out.println(e);

        JournalEntry found = SearchUtils.binarySearchByDate(all, LocalDate.of(2026, 3, 11));
        System.out.println("Binary search hit: " + found);

        ActionManager actions = new ActionManager();
        StringBuilder title = new StringBuilder("Kyoto Temples");
        actions.perform(new com.example.dsafinals.action.Action() {
            public void redo() { title.append(" (edited)"); }
            public void undo() { title.setLength(title.length() - " (edited)".length()); }
        });
        System.out.println("After edit: " + title);
        actions.undo();
        System.out.println("After undo: " + title);

        ImageLoadQueue queue = new ImageLoadQueue();
        for (int i = 1; i <= 3; i++) {
            int n = i;
            queue.submit(() -> System.out.println("Loaded image " + n));
        }
        Thread.sleep(200);
    }
}
