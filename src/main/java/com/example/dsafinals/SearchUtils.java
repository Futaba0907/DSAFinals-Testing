package com.example.dsafinals.util;

import com.example.dsafinals.model.JournalEntry;

import java.time.LocalDate;

public class SearchUtils {

    // entries must already be sorted ascending by date (see SortUtils.byDate())
    public static JournalEntry binarySearchByDate(JournalEntry[] entries, LocalDate targetDate) {
        int low = 0, high = entries.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = entries[mid].getDate().compareTo(targetDate);
            if (cmp == 0) return entries[mid];
            if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return null;
    }

    // index of the closest entry on or before targetDate, useful when there is no exact match
    public static int floorIndexByDate(JournalEntry[] entries, LocalDate targetDate) {
        int low = 0, high = entries.length - 1, result = -1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (!entries[mid].getDate().isAfter(targetDate)) {
                result = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return result;
    }
}
