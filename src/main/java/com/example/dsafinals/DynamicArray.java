package com.example.dsafinals.datastructures;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DynamicArray<T> implements Iterable<T>, Serializable {
    private Object[] data;
    private int size = 0;

    public DynamicArray() {
        this(8);
    }

    public DynamicArray(int capacity) {
        data = new Object[Math.max(capacity, 1)];
    }

    public void add(T item) {
        if (size == data.length) grow();
        data[size++] = item;
    }

    public void removeAt(int index) {
        checkIndex(index);
        for (int i = index; i < size - 1; i++) data[i] = data[i + 1];
        data[--size] = null;
    }

    public boolean remove(T item) {
        for (int i = 0; i < size; i++) {
            if (data[i] != null && data[i].equals(item)) {
                removeAt(i);
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkIndex(index);
        return (T) data[index];
    }

    public void set(int index, T item) {
        checkIndex(index);
        data[index] = item;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @SuppressWarnings("unchecked")
    public T[] toArray(T[] template) {
        return (T[]) Arrays.copyOf(data, size, template.getClass());
    }

    private void grow() {
        data = Arrays.copyOf(data, data.length * 2);
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                return (T) data[cursor++];
            }
        };
    }
}
