package me.thm64.worker;

import me.thm64.utility.SkagwayImage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class InetWorkerTest {

    @BeforeMethod
    public void setUp() throws Exception {
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testRun1() throws Exception {
        ArrayBlockingQueue<SkagwayImage> queue = new ArrayBlockingQueue<SkagwayImage>(10);

        InetWorker worker = new InetWorker(0, queue, InetAddress.getByName("127.0.0.1"), 54321);
        worker.stop = false;

        Thread workerThread = new Thread(worker);

        workerThread.start();

        int frameCount = 0;

        long lastFrameSeq = -1;
        while (frameCount < 200) {
            SkagwayImage image = queue.poll(1, TimeUnit.SECONDS);
            if (image == null) continue;

            long frameSeq = image.frameSeq;
            System.out.println("Receive frame " + frameSeq);

            Assert.assertTrue(frameSeq > lastFrameSeq);
            Assert.assertEquals(image.sourceId, worker.getId());

            lastFrameSeq = frameSeq;
            frameCount++;

            long imageSeq = (frameSeq % 700) + 1;
            File image_file = new File(String.format("test/me/thm64/images/%06d.jpeg", imageSeq));
            FileInputStream in = new FileInputStream(image_file);

            byte[] image_data = in.readAllBytes();

            for (int i = 0; i < image_data.length; i++) {
                Assert.assertEquals(image_data[i], image.data[i]);
            }

        }

        worker.stop = true;
        workerThread.join();

        Assert.assertTrue(worker.timeOffset.valid);
        Assert.assertTrue(Math.abs(worker.timeOffset.val) < 50);
    }
}