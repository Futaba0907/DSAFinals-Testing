package com.example.dsafinals.datastructures;

/**
 * Custom Queue implementation using a circular array.
 * Used for memory recall processing and sequential image loading tasks.
 *
 * DSA Concept: Queue (FIFO - First In, First Out)
 */
public class AppQueue<T> {
    private Object[] data;
    private int head;
    private int tail;
    private int size;
    private int capacity;
    private static final int DEFAULT_CAPACITY = 50;

    public AppQueue() {
        this(DEFAULT_CAPACITY);
    }

    public AppQueue(int capacity) {
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.head = 0;
        this.tail = 0;
        this.size = 0;
    }

    /** Add an item to the back of the queue. */
    public void enqueue(T item) {
        if (size == capacity) grow();
        data[tail] = item;
        tail = (tail + 1) % capacity;
        size++;
    }

    /** Remove and return the front item. */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) throw new RuntimeException("Queue is empty");
        T item = (T) data[head];
        data[head] = null;
        head = (head + 1) % capacity;
        size--;
        return item;
    }

    /** View the front item without removing it. */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new RuntimeException("Queue is empty");
        return (T) data[head];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) data[i] = null;
        head = 0;
        tail = 0;
        size = 0;
    }

    private void grow() {
        int newCapacity = capacity * 2;
        Object[] newData = new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            newData[i] = data[(head + i) % capacity];
        }
        data = newData;
        head = 0;
        tail = size;
        capacity = newCapacity;
    }
}
