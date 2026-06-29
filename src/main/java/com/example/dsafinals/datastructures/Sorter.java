package com.example.dsafinals.datastructures;

import com.example.dsafinals.models.JournalEntry;
import com.example.dsafinals.models.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * Sorting algorithms for journal entries and photos.
 *
 * DSA Concepts:
 * - Merge Sort (stable, good for journal entries by date/title)
 * - Quick Sort (fast average case, good for large photo collections)
 */
public class Sorter {

    // ─────────────────────────────────────────────
    // MERGE SORT — for Journal Entries
    // ─────────────────────────────────────────────

    /** Sort journal entries by date (ascending) using Merge Sort. */
    public static List<JournalEntry> mergeSortByDate(List<JournalEntry> entries) {
        if (entries.size() <= 1) return entries;

        int mid = entries.size() / 2;
        List<JournalEntry> left = mergeSortByDate(new ArrayList<>(entries.subList(0, mid)));
        List<JournalEntry> right = mergeSortByDate(new ArrayList<>(entries.subList(mid, entries.size())));

        return mergeByDate(left, right);
    }

    private static List<JournalEntry> mergeByDate(List<JournalEntry> left, List<JournalEntry> right) {
        List<JournalEntry> result = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            if (!left.get(i).getDate().isAfter(right.get(j).getDate())) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }

        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        return result;
    }

    /** Sort journal entries by title (alphabetical) using Merge Sort. */
    public static List<JournalEntry> mergeSortByTitle(List<JournalEntry> entries) {
        if (entries.size() <= 1) return entries;

        int mid = entries.size() / 2;
        List<JournalEntry> left = mergeSortByTitle(new ArrayList<>(entries.subList(0, mid)));
        List<JournalEntry> right = mergeSortByTitle(new ArrayList<>(entries.subList(mid, entries.size())));

        return mergeByTitle(left, right);
    }

    private static List<JournalEntry> mergeByTitle(List<JournalEntry> left, List<JournalEntry> right) {
        List<JournalEntry> result = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            if (left.get(i).getTitle().compareToIgnoreCase(right.get(j).getTitle()) <= 0) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }

        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        return result;
    }

    // ─────────────────────────────────────────────
    // QUICK SORT — for Photos
    // ─────────────────────────────────────────────

    /** Sort photos by date taken (ascending) using Quick Sort. */
    public static void quickSortPhotosByDate(List<Photo> photos, int low, int high) {
        if (low < high) {
            int pivotIndex = partitionByDate(photos, low, high);
            quickSortPhotosByDate(photos, low, pivotIndex - 1);
            quickSortPhotosByDate(photos, pivotIndex + 1, high);
        }
    }

    private static int partitionByDate(List<Photo> photos, int low, int high) {
        Photo pivot = photos.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (!photos.get(j).getDateTaken().isAfter(pivot.getDateTaken())) {
                i++;
                Photo temp = photos.get(i);
                photos.set(i, photos.get(j));
                photos.set(j, temp);
            }
        }

        Photo temp = photos.get(i + 1);
        photos.set(i + 1, photos.get(high));
        photos.set(high, temp);
        return i + 1;
    }

    /** Sort photos by file name using Quick Sort. */
    public static void quickSortPhotosByName(List<Photo> photos, int low, int high) {
        if (low < high) {
            int pivotIndex = partitionByName(photos, low, high);
            quickSortPhotosByName(photos, low, pivotIndex - 1);
            quickSortPhotosByName(photos, pivotIndex + 1, high);
        }
    }

    private static int partitionByName(List<Photo> photos, int low, int high) {
        Photo pivot = photos.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (photos.get(j).getFileName().compareToIgnoreCase(pivot.getFileName()) <= 0) {
                i++;
                Photo temp = photos.get(i);
                photos.set(i, photos.get(j));
                photos.set(j, temp);
            }
        }

        Photo temp = photos.get(i + 1);
        photos.set(i + 1, photos.get(high));
        photos.set(high, temp);
        return i + 1;
    }
}
