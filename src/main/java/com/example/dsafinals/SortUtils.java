package com.example.dsafinals.util;

import com.example.dsafinals.model.JournalEntry;

import java.util.Arrays;
import java.util.Comparator;

public class SortUtils {

    public static void mergeSort(JournalEntry[] entries, Comparator<JournalEntry> comparator) {
        if (entries.length < 2) return;
        int mid = entries.length / 2;
        JournalEntry[] left = Arrays.copyOfRange(entries, 0, mid);
        JournalEntry[] right = Arrays.copyOfRange(entries, mid, entries.length);
        mergeSort(left, comparator);
        mergeSort(right, comparator);
        merge(entries, left, right, comparator);
    }

    private static void merge(JournalEntry[] entries, JournalEntry[] left, JournalEntry[] right,
                               Comparator<JournalEntry> comparator) {
        int i = 0, j = 0, k = 0;
        while (i < left.length && j < right.length) {
            entries[k++] = comparator.compare(left[i], right[j]) <= 0 ? left[i++] : right[j++];
        }
        while (i < left.length) entries[k++] = left[i++];
        while (j < right.length) entries[k++] = right[j++];
    }

    public static void quickSort(JournalEntry[] entries, Comparator<JournalEntry> comparator) {
        quickSort(entries, 0, entries.length - 1, comparator);
    }

    private static void quickSort(JournalEntry[] entries, int low, int high, Comparator<JournalEntry> comparator) {
        if (low >= high) return;
        int pivotIndex = partition(entries, low, high, comparator);
        quickSort(entries, low, pivotIndex - 1, comparator);
        quickSort(entries, pivotIndex + 1, high, comparator);
    }

    private static int partition(JournalEntry[] entries, int low, int high, Comparator<JournalEntry> comparator) {
        JournalEntry pivot = entries[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (comparator.compare(entries[j], pivot) <= 0) {
                i++;
                swap(entries, i, j);
            }
        }
        swap(entries, i + 1, high);
        return i + 1;
    }

    private static void swap(JournalEntry[] entries, int a, int b) {
        JournalEntry temp = entries[a];
        entries[a] = entries[b];
        entries[b] = temp;
    }

    public static Comparator<JournalEntry> byDate() {
        return Comparator.comparing(JournalEntry::getDate);
    }

    public static Comparator<JournalEntry> byTitle() {
        return Comparator.comparing(JournalEntry::getTitle);
    }
}
