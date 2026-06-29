package com.example.dsafinals.datastructures;

/**
 * Custom Stack implementation using an array.
 * Used for Undo/Redo functionality.
 *
 * DSA Concept: Stack (LIFO - Last In, First Out)
 */
public class AppStack<T> {
    private Object[] data;
    private int top;
    private int capacity;
    private static final int DEFAULT_CAPACITY = 50;

    public AppStack() {
        this(DEFAULT_CAPACITY);
    }

    public AppStack(int capacity) {
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.top = -1;
    }

    /** Push an item onto the stack. Grows if full. */
    public void push(T item) {
        if (top == capacity - 1) {
            grow();
        }
        data[++top] = item;
    }

    /** Remove and return the top item. */
    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) throw new RuntimeException("Stack is empty");
        T item = (T) data[top];
        data[top--] = null;
        return item;
    }

    /** View the top item without removing it. */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new RuntimeException("Stack is empty");
        return (T) data[top];
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public int size() {
        return top + 1;
    }

    public void clear() {
        for (int i = 0; i <= top; i++) data[i] = null;
        top = -1;
    }

    private void grow() {
        capacity *= 2;
        Object[] newData = new Object[capacity];
        System.arraycopy(data, 0, newData, 0, top + 1);
        data = newData;
    }
}
