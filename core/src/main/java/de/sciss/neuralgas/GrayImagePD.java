package de.sciss.neuralgas;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * @author Hanns Holger Rutz
 */
public class GrayImagePD implements PD {
  private final BufferedImage img;
  private final int w;
  private final int h;
  private final int numPixels;
  private final double[] dots;

  public GrayImagePD(BufferedImage img, boolean invert) {
    this.img  = img;
    w         = img.getWidth();
    h         = img.getHeight();
    numPixels = w * h;

    final double[] _dots = new double[numPixels];
    double sum = 0.0;
    int i = 0;
    int x = 0;
    while (x < w) {
      int y = 0;
      while (y < h) {
        int rgb     = img.getRGB(x, y);
        double red     = ((rgb & 0xFF0000) >> 16) / 255.0;
        double green   = ((rgb & 0x00FF00) >>  8) / 255.0;
        double blue    = ((rgb & 0x00FF00) >>  8) / 255.0;
        double lum     = 0.2126 * red + 0.7152 * green + 0.0722 * blue;
        double value   = invert ? 1.0 - lum : lum;
        sum += value;
        _dots[i] = sum;
        i += 1;
        y += 1;
      }
      x += 1;
    }
    i = 0;
    final double mul = 1.0 / sum;
    while (i < numPixels) {
      _dots[i] *= mul;
      i += 1;
    }

    dots = _dots;
  }

  public int getNumPixels() { return numPixels; }

  @Override
  public void getSignal(ComputeGNG compute) {
    final int wi    = compute.panelWidth;
    final int hi    = compute.panelHeight;
    final double[] _dots = dots;
    final int i0    = Arrays.binarySearch(_dots, compute.random());
    final int i1    = (i0 >= 0)           ? i0 : -(i0 - 1);
    final int dot   = (i1 < _dots.length) ? i1 : _dots.length - 1;
    final int _h    = h;
    final int xIn   = dot / _h;
    final int yIn   = dot % _h;
    compute.SignalX = ((float) (xIn * wi)) /  w;
    compute.SignalY = ((float) (yIn * hi)) / _h;
  }

  @Override
  public void draw(ComputeGNG compute, PanelLike panel, Graphics g, Dimension d) {
    final int wi = d.width;
    final int hi = d.height;
    g.drawImage(img, 0, 0, wi, hi, null);
  }

  @Override
  public String getName() {
    return "GImage";
  }

  @Override
  public int ordinal() {
    return -1;
  }
}