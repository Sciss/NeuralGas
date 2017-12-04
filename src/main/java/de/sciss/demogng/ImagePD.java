package de.sciss.demogng;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * @author Hanns Holger Rutz
 */
public class ImagePD implements PD {
    private final BufferedImage img;
    private final long[] dots;
    private final int    numDots;
    private final int    imgW, imgH;

    public int getNumDots() { return numDots; }

    public ImagePD(BufferedImage img, boolean invert) {
        super();
        final int w = img.getWidth();
        final int h = img.getHeight();
        final int numPixels = w * h;
        final long[] _dots = new long[numPixels];
        int _numDots = 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                final int rgb       = img.getRGB(x, y);
                final int value     = (((rgb & 0xFF0000) >> 16) + ((rgb & 0xFF00) >> 8) + (rgb & 0x0000FF)) / 3;
                final boolean isDot = value > /* <= */ 0x7F;
                if (isDot ^ invert) {
                    _dots[_numDots] = (((long) x) << 32) | (y & 0xFFFFFFFFL);
                    _numDots++;
                }
            }
        }
        this.img    = img;
        dots        = _dots;
        numDots     = _numDots;
        imgW        = w;
        imgH        = h;
    }

    @Override
    public void getSignal(final ComputeGNG compute) {
        final int wi    = compute.panelWidth;
        final int hi    = compute.panelHeight;
        final long dot  = dots[(int) (Math.random() * numDots)];
        final int xIn   = (int) (dot >>> 32);
        final int yIn   = (int) dot;

        compute.SignalX = (float) (xIn * wi) / imgW;
        compute.SignalY = (float) (yIn * hi) / imgH;
    }

    @Override
    public void draw(final PanelGNG panel, final Graphics g, Dimension d) {
        final int wi = d.width; // panelWidth;
        final int hi = d.height; // panelHeight;
        g.drawImage(img, 0, 0, wi, hi, null);
    }

    @Override
    public String getName() {
        return "Image";
    }

    @Override
    public int ordinal() {
        return -1;
    }
}