package me.thm64.controller;


import me.thm64.controller.callback.OnImageArrival;
import me.thm64.utility.SkagwayImage;
import me.thm64.worker.SkagwayWorker;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SkagwayController {
    private final static int IN_QUEUE_LEN = 10;

    private class ImageForwardRunnable implements Runnable {
        private final SkagwayController controller;
        public boolean stop;
        ImageForwardRunnable(SkagwayController controller) {
            this.controller = controller;
            this.stop = true;
        }
        public void run() {
            while (!this.stop) {
                SkagwayImage image = null;
                try {
                    image = this.controller.inQueue.poll(500, TimeUnit.MILLISECONDS);
                }
                catch (InterruptedException e) {
                    //TODO What is happening?
                    System.err.println("Warn: SkagwayThread: Receive interrupt");
                    continue;
                }

                if (image == null) continue;

                if (controller.onImageArrival != null) {
                    controller.onImageArrival.callback(image);
                }
            }
        }
    }

    private class WorkerRecord {
        final public Thread thread;
        final public SkagwayWorker worker;

        WorkerRecord(Thread thread, SkagwayWorker worker) {
            this.thread = thread;
            this.worker = worker;
        }
    }

    // Ownership => shared by image push thread and workers threads
    private final ArrayBlockingQueue<SkagwayImage> inQueue;

    // Ownership => Control Thread
    private final HashMap<Integer, WorkerRecord> workers;
    private volatile OnImageArrival onImageArrival;
    private Thread imageForwardThread;
    final private ImageForwardRunnable imageForwardRunnable;

    public SkagwayController() {
        this.inQueue = new ArrayBlockingQueue<SkagwayImage>(SkagwayController.IN_QUEUE_LEN);
        this.workers = new HashMap<Integer, WorkerRecord>();
        this.onImageArrival = null;
        this.imageForwardThread = null;
        this.imageForwardRunnable = new ImageForwardRunnable(this);
    }

    public void addWorker(SkagwayWorker worker) {
        int id = worker.getId();

        if (this.workers.get(id) != null) {
            //TODO Handle Error
            System.err.println("Error: addWorker: Worker id exists");
            return;
        }

        Thread thread = new Thread(worker);
        worker.stop = false;

        thread.start();

        WorkerRecord record = new WorkerRecord(thread, worker);
        workers.put(id, record);

        //TODO WorkerChangeCallback
    }

    public boolean removeWorker(int id) {
        WorkerRecord record = this.workers.remove(id);

        if (record == null)
            return false;

        record.worker.stop = true;
        while (true) {
            try {
                record.thread.join();
            }
            catch (InterruptedException e) {
                //TODO What is happening?
                System.err.println("Warn: SkagwayThread: Receive interrupt");
                continue;
            }
            break;
        }

        //TODO WorkerChangeCallback
        return true;
    }

    public ArrayBlockingQueue<SkagwayImage> getInQueue() {
        return this.inQueue;
    }

    public void setOnImageArrival(OnImageArrival callbackObject) {
        this.onImageArrival = callbackObject;
    }

    public int getWorkerNumber() {
        return this.workers.size();
    }

    public void start() {
        if (imageForwardThread != null) return;

        this.imageForwardThread = new Thread(imageForwardRunnable);

        imageForwardRunnable.stop = false;
        imageForwardThread.start();
    }

    public void stop() {
        if (imageForwardThread == null) return;

        this.imageForwardRunnable.stop = true;

        while (true) {
            try {
                this.imageForwardThread.join();
            } catch (InterruptedException e) {
                //TODO What is happening?
                System.err.println("Warn: SkagwayThread: Receive interrupt");
                continue;
            }
            break;
        }
        this.imageForwardThread = null;
    }

}
