package me.thm64.worker.assembler;

import me.thm64.protocol.DataSegment;
import me.thm64.utility.SkagwayImage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import static java.lang.Math.abs;

public class AssemblerTest {

    private DataSegment openFromFile(int frameSeq, int frag) {
        File cap = new File("test/me/thm64/capture/frame_" + frameSeq + "_frag_" + Integer.toString(frag) + ".cap");
        FileInputStream in;
        int len = 0;
        byte[] capData = null;
        try {
            in = new FileInputStream(cap);
            len = in.available();
            capData = in.readAllBytes();
            in.close();
        } catch (IOException e) {
            return null;
        }

        return new DataSegment(capData, len);
    }

    private void feedFrame(DataSegment[] caps, Assembler assembler, int totalFrags) {
        for (int i = 0; i < totalFrags; i++) {
            boolean res = assembler.feed(caps[i]);
            Assert.assertEquals(res, true);
        }
        Assert.assertEquals(assembler.available, true);
    }

    @Test
    public void testFeed() throws Exception {

        Assembler test1, test2, test3;

        DataSegment[] caps = new DataSegment[200];
        for (int i = 0; i < 60; i++) {
            caps[i] = openFromFile(123, i);
        }

        test1 = new Assembler();
        feedFrame(caps, test1, 60);
        Assert.assertEquals(test1.available, true);

        Random r = new Random();
        for (int i = 0; i < 200; i++) {
            int idxA = abs(r.nextInt()) % 60;
            int idxB = abs(r.nextInt()) % 60;
            DataSegment temp = caps[idxA];
            caps[idxA] = caps[idxB];
            caps[idxB] = temp;
        }

        test2 = new Assembler();
        feedFrame(caps, test2, 60);
        Assert.assertEquals(test2.available, true);

        long frameSeq = caps[0].getFrameSeq();
        DataSegment outOrderMessage = openFromFile(122, 0);
        boolean res = test1.feed(outOrderMessage);
        Assert.assertEquals(res, false);
        res = test2.feed(outOrderMessage);
        Assert.assertEquals(res, false);

        test3 = new Assembler();
    }

    @Test
    public void testGet() throws Exception {
        DataSegment[][] caps = new DataSegment[130][200];
        int[] fragsSize = new int[200];
        for (int j = 0; j < 130; j++) {
            for (int i = 0; i < 200; i++) {
                DataSegment res = openFromFile(j, i);
                if (res == null) {
                    fragsSize[j] = i;
                    break;
                }
                caps[j][i] = res;
            }
        }
        File expectedImg = new File("test/me/thm64/images/000124.jpeg");
        FileInputStream in = new FileInputStream(expectedImg);
        int imgLen = in.available();
        byte[] expectedImgData = in.readAllBytes();

        Assembler test1 = new Assembler();
        feedFrame(caps[123], test1, fragsSize[123]);

        SkagwayImage image = test1.get();
        Assert.assertEquals(image.frameSeq, 123);
        Assert.assertEquals(image.size, imgLen);
        Assert.assertEquals(image.type, SkagwayImage.imageType.jpeg);
        for (int i = 0; i < image.size; i++) {
            Assert.assertEquals(image.data[i], expectedImgData[i]);
        }

        Assert.assertEquals(test1.get(), null);

        expectedImg = new File("test/me/thm64/images/000126.jpeg");
        in = new FileInputStream(expectedImg);
        imgLen = in.available();
        expectedImgData = in.readAllBytes();

        feedFrame(caps[124], test1, fragsSize[124]);
        feedFrame(caps[125], test1, fragsSize[125]);
        image = test1.get();
        Assert.assertEquals(image.frameSeq, 125);
        Assert.assertEquals(image.size, imgLen);
        Assert.assertEquals(image.type, SkagwayImage.imageType.jpeg);
        for (int i = 0; i < image.size; i++) {
            Assert.assertEquals(image.data[i], expectedImgData[i]);
        }
        Assert.assertEquals(test1.get(), null);

        Assembler test2 = new Assembler();

        test2.feed(caps[4][2]);
        Assert.assertEquals(test2.get(), null);
    }

}