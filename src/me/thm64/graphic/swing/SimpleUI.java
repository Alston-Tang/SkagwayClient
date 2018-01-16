package me.thm64.graphic.swing;


import me.thm64.controller.SkagwayController;
import me.thm64.controller.callback.OnImageArrival;
import me.thm64.utility.SkagwayImage;
import me.thm64.worker.InetWorker;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import javax.swing.JFrame;

import static java.lang.System.exit;
import static java.lang.System.in;

public class SimpleUI extends JFrame {
    private DisplayPanel panel;

    public SimpleUI() {
        initUI();
    }

    public void updateImage(SkagwayImage image) {
        this.panel.loadImage(image);
        this.getContentPane().repaint();
    }

    private void initUI() {
        setTitle("Simple UI");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        panel = new DisplayPanel();
        this.getContentPane().add(panel);
    }

    static private class UIWrapper {
        SimpleUI ui;
    }

    static private class UIMain implements Runnable {
        UIWrapper wrapper = null;

        UIMain(UIWrapper wrapper) {
            this.wrapper = wrapper;
        }

        public void run() {
            wrapper.ui = new SimpleUI();
            wrapper.ui.setVisible(true);
        }
    }

    static private class ImageHandler implements OnImageArrival {
        UIWrapper wrapper;
        ImageHandler(UIWrapper wrapper) {
            this.wrapper = wrapper;
        }
        @Override
        public void callback(SkagwayImage image) {
            EventQueue.invokeLater(() -> {
                this.wrapper.ui.updateImage(image);
            });
        }
    }

    public static void main(String[] args) throws Exception{
        UIWrapper wrapper = new UIWrapper();
        UIMain uiMain = new UIMain(wrapper);
        EventQueue.invokeLater(uiMain);

        SkagwayController controller = new SkagwayController();
        InetWorker worker = new InetWorker(0, controller.getInQueue(), InetAddress.getByName("192.168.123.1"), 12345);
        ImageHandler handler = new ImageHandler(wrapper);

        controller.setOnImageArrival(handler);
        controller.start();
        controller.addWorker(worker);
    }
}
