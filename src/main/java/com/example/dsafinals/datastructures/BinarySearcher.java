package com.example.dsafinals.datastructures;

import com.example.dsafinals.models.JournalEntry;
import com.example.dsafinals.models.Photo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Binary Search implementation for fast date-based retrieval.
 * Used in the Memory Recall feature.
 *
 * IMPORTANT: The list must be sorted by date before calling these methods.
 * Use Sorter.mergeSortByDate() first.
 *
 * DSA Concept: Binary Search O(log n) vs Linear Search O(n)
 */
public class BinarySearcher {

    /**
     * Find all journal entries matching a specific date.
     * Returns all matches (there may be multiple entries on the same day).
     */
    public static List<JournalEntry> searchEntriesByDate(List<JournalEntry> sortedEntries, LocalDate targetDate) {
        List<JournalEntry> results = new ArrayList<>();
        if (sortedEntries.isEmpty()) return results;

        // Binary search for any entry with this date
        int index = binarySearchEntryDate(sortedEntries, targetDate);
        if (index == -1) return results;

        // Expand left and right to find ALL entries on this date
        int left = index;
        while (left > 0 && sortedEntries.get(left - 1).getDate().equals(targetDate)) left--;

        int right = index;
        while (right < sortedEntries.size() - 1 && sortedEntries.get(right + 1).getDate().equals(targetDate)) right++;

        for (int i = left; i <= right; i++) {
            results.add(sortedEntries.get(i));
        }

        return results;
    }

    /**
     * Find all journal entries within a date range (e.g. "this week last year").
     * Used for memory recall: "on this day" or "this week X years ago".
     */
    public static List<JournalEntry> searchEntriesInRange(List<JournalEntry> sortedEntries,
                                                           LocalDate startDate, LocalDate endDate) {
        List<JournalEntry> results = new ArrayList<>();

        // Binary search for the start position
        int startIndex = lowerBound(sortedEntries, startDate);

        for (int i = startIndex; i < sortedEntries.size(); i++) {
            LocalDate date = sortedEntries.get(i).getDate();
            if (date.isAfter(endDate)) break;
            results.add(sortedEntries.get(i));
        }

        return results;
    }

    /**
     * Find all photos within a date range (for memory recall of photos).
     */
    public static List<Photo> searchPhotosInRange(List<Photo> sortedPhotos,
                                                   LocalDate startDate, LocalDate endDate) {
        List<Photo> results = new ArrayList<>();

        int startIndex = lowerBoundPhotos(sortedPhotos, startDate);

        for (int i = startIndex; i < sortedPhotos.size(); i++) {
            LocalDate date = sortedPhotos.get(i).getDateTaken();
            if (date.isAfter(endDate)) break;
            results.add(sortedPhotos.get(i));
        }

        return results;
    }

    // ─── Private helpers ───────────────────────────────────────

    private static int binarySearchEntryDate(List<JournalEntry> entries, LocalDate target) {
        int low = 0, high = entries.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            LocalDate midDate = entries.get(mid).getDate();

            if (midDate.equals(target)) return mid;
            else if (midDate.isBefore(target)) low = mid + 1;
            else high = mid - 1;
        }

        return -1; // Not found
    }

    /** Find the first index where date >= startDate. */
    private static int lowerBound(List<JournalEntry> entries, LocalDate startDate) {
        int low = 0, high = entries.size();

        while (low < high) {
            int mid = (low + high) / 2;
            if (entries.get(mid).getDate().isBefore(startDate)) low = mid + 1;
            else high = mid;
        }

        return low;
    }

    private static int lowerBoundPhotos(List<Photo> photos, LocalDate startDate) {
        int low = 0, high = photos.size();

        while (low < high) {
            int mid = (low + high) / 2;
            if (photos.get(mid).getDateTaken().isBefore(startDate)) low = mid + 1;
            else high = mid;
        }

        return low;
    }
}
