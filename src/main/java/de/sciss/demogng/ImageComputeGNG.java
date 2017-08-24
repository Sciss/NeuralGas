package de.sciss.demogng;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Hanns Holger Rutz
 */
public class ImageComputeGNG extends ComputeGNG {
    private final BufferedImage img;
    private final long[] dots;
    private final int    numDots;
    private final int    imgW, imgH;

    public ImageComputeGNG(DemoGNG graph, BufferedImage img) {
        super(graph);
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
                if (isDot) {
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
    protected void getSignal(PD pd) {
        final int wi    = panelWidth;
        final int hi    = panelHeight;
        final long dot  = dots[(int) (Math.random() * numDots)];
        final int xIn   = (int) (dot >>> 32);
        final int yIn   = (int) dot;

        SignalX = xIn * wi / imgW;
        SignalY = yIn * hi / imgH;
    }

    @Override
    protected void drawPD(final Graphics g, Dimension d) {
        final int wi = panelWidth;
        final int hi = panelHeight;
        g.drawImage(img, 0, 0, wi, hi, null);
    }
}