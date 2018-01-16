package me.thm64.controller;

import me.thm64.controller.callback.OnImageArrival;
import me.thm64.utility.SkagwayImage;
import me.thm64.worker.InetWorker;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

import static java.lang.System.exit;
import static java.lang.System.in;

public class SkagwayControllerTest {

    SkagwayController controller;

    @BeforeMethod
    public void setUp() throws Exception {
        controller = new SkagwayController();
        controller.start();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        controller.stop();
    }

    @Test
    public void testAddRemoveWorker() throws Exception {
        Thread.sleep(10000);

        InetWorker worker = new InetWorker(0, controller.getInQueue(), InetAddress.getByName("127.0.0.1"), 54321);
        Assert.assertFalse(worker.getTimeOffset().valid);
        controller.addWorker(worker);
        Assert.assertEquals(controller.getWorkerNumber(), 1);


        Assert.assertTrue(controller.removeWorker(0));
        Assert.assertEquals(controller.getWorkerNumber(), 0);
        Assert.assertTrue(worker.getTimeOffset().valid);
    }

    @Test
    public void testRemoveWorker() throws Exception {
    }

    @Test
    public void testSetOnImageArrival() throws Exception {

        class TestOnImageArrival implements OnImageArrival {
            public int imageCount = 0;
            @Override
            public void callback(SkagwayImage image) {
                imageCount++;
                long frameSeq = image.frameSeq;
                long imageSeq = (frameSeq % 700) + 1;
                File image_file = new File(String.format("test/me/thm64/images/%06d.jpeg", imageSeq));
                byte[] image_data = null;
                try {
                    FileInputStream in = new FileInputStream(image_file);
                    image_data = in.readAllBytes();
                }
                catch (IOException e) {
                    System.err.println("Can not open file " + String.format("test/me/thm64/images/%06d.jpeg", imageSeq));
                    exit(-1);
                }

                for (int i = 0; i < image.size; i++) {
                    Assert.assertEquals(image_data[i], image.data[i]);
                }

                try {
                    in.close();
                }
                catch (IOException e) {
                    System.err.println("Can not close file " + String.format("test/me/thm64/images/%06d.jpeg", imageSeq));
                    exit(-1);
                }
            }
        }

        InetWorker worker = new InetWorker(1, controller.getInQueue(), InetAddress.getByName("127.0.0.1"), 54321);
        TestOnImageArrival callback = new TestOnImageArrival();

        controller.addWorker(worker);
        controller.setOnImageArrival(callback);

        while (!worker.getTimeOffset().valid) {
            Thread.sleep(500);
        }

        Thread.sleep(2000);

        controller.setOnImageArrival(null);
        controller.removeWorker(1);

        Assert.assertTrue(callback.imageCount > 50);

    }
}