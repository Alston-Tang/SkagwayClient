package me.thm64.protocol;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.testng.Assert.*;

public class DataSegmentTest {

    class openResult {
        int len;
        DataSegment message;
    }

    private openResult openFromFile(int frag) {
        openResult rv = new openResult();
        File cap = new File("test/me/thm64/capture/frame_123_frag_" + Integer.toString(frag) + ".cap");
        FileInputStream in;
        byte[] capData = null;
        try {
            in = new FileInputStream(cap);
            rv.len = in.available();
            capData = in.readAllBytes();
        } catch (IOException e) {
            System.err.println("Can not open file " + "test/me/thm64/capture/frame_123_frag_" + Integer.toString(frag) + ".cap");
            System.exit(-1);
        }

        rv.message = new DataSegment(capData, rv.len);
        return rv;
    }

    @BeforeMethod
    public void setUp() throws Exception {
    }
    @AfterMethod
    public void tearDown() throws Exception {
    }
    @Test
    public void testDataSegment() throws Exception {
        byte[] expectedImg = {'a','b','c','d'};
        long time = 0xF356F3B3L;
        long frameSeq = 0X000003FBL;
        int totalFrag = 0x00A0;
        int curFrag = 0x0000;
        DataSegment message= new DataSegment(time, frameSeq, totalFrag, curFrag, expectedImg);

        byte[] img = new byte[expectedImg.length];

        Assert.assertEquals(message.getTime(), time);
        Assert.assertEquals(message.getFrameSeq(), frameSeq);
        Assert.assertEquals(message.getTotalFrag(), totalFrag);
        Assert.assertEquals(message.getCurFrag(), curFrag);
        Assert.assertEquals(message.getDataLen(), expectedImg.length);
        message.copyImg(img, 0, expectedImg.length);
        Assert.assertEquals(expectedImg, img);
    }
    @Test
    public void testGetTime() throws Exception {
        for (int i = 0; i < 60; i++) {
            DataSegment message = openFromFile(i).message;
        }
    }

    @Test
    public void testGetFrameSeq() throws Exception {
        for (int i = 0; i < 60; i++) {
            DataSegment message = openFromFile(i).message;
            Assert.assertEquals(message.getFrameSeq(), 123L);
        }
    }

    @Test
    public void testGetTotalFrag() throws Exception {
        for (int i = 0; i < 60; i++) {
            DataSegment message = openFromFile(i).message;
            Assert.assertEquals(message.getTotalFrag(), 60);
        }
    }

    @Test
    public void testGetCurFrag() throws Exception {
        for (int i = 0; i < 60; i++) {
            DataSegment message = openFromFile(i).message;
            Assert.assertEquals(message.getCurFrag(), i);
        }
    }

    @Test
    public void testGetDataLen() throws Exception {
        for (int i = 0; i < 60; i++) {
            openResult res = openFromFile(i);
            Assert.assertEquals(res.message.getDataLen(), res.len - SkagwayMessage.DATA_SEGMENT_HEADER_LENGTH);
        }
    }

    @Test
    public void testCopyData() throws Exception {
        byte[] img = new byte[DataSegment.FRAG_MAX_SIZE * 500];

        for (int i = 0; i < 60; i++) {
            int offset = i * DataSegment.FRAG_MAX_SIZE;
            DataSegment message = openFromFile(i).message;
            message.copyImg(img, offset, message.getDataLen());
        }

        File expectedImg = new File("test/me/thm64/images/000124.jpeg");
        FileInputStream in = new FileInputStream(expectedImg);
        int imgLen = in.available();
        byte[] expectedImgData = in.readAllBytes();
        for (int i = 0; i < imgLen; i++) {
            Assert.assertEquals(img[i], expectedImgData[i]);
        }
    }

}