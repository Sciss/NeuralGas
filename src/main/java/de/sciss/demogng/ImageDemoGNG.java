package de.sciss.demogng;

import java.awt.image.BufferedImage;

/**
 * @author Hanns Holger Rutz
 */
public class ImageDemoGNG extends DemoGNG {
    private final BufferedImage img;
    private final boolean invert;

    public ImageDemoGNG(BufferedImage img, boolean invert) {
        super();
        this.img = img;
        this.invert = invert;
    }

    @Override
    protected ComputeGNG createComputation() {
        return new ImageComputeGNG(this, img, invert);
    }
}
