package me.thm64.graphic.swing;

import me.thm64.utility.SkagwayImage;

import javax.swing.*;
import java.awt.*;

public class DisplayPanel extends JPanel {
    private volatile Image image = null;

    public void loadImage(SkagwayImage image) {
        this.image = new ImageIcon(image.data).getImage();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (image == null) return;

        Dimension size = this.getSize();
        g.drawImage(this.image, 0, 0, size.width, size.height, this);
    }
}
