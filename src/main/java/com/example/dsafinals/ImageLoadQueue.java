package com.example.dsafinals.service;

import com.example.dsafinals.datastructures.LinkedQueue;

public class ImageLoadQueue {
    private final LinkedQueue<Runnable> tasks = new LinkedQueue<>();
    private boolean running = false;

    public synchronized void submit(Runnable task) {
        tasks.enqueue(task);
        processNext();
    }

    private synchronized void processNext() {
        if (running || tasks.isEmpty()) return;
        running = true;
        Runnable task = tasks.dequeue();
        Thread worker = new Thread(() -> {
            try {
                task.run();
            } finally {
                onTaskFinished();
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    private synchronized void onTaskFinished() {
        running = false;
        processNext();
    }
}
