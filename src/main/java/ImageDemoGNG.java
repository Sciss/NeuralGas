import java.awt.image.BufferedImage;

/**
 * @author Hanns Holger Rutz
 */
public class ImageDemoGNG extends DemoGNG {
    private final BufferedImage img;

    public ImageDemoGNG(BufferedImage img) {
        super();
        this.img = img;
    }

    @Override
    protected ComputeGNG createComputation() {
        return new ImageComputeGNG(this, img);
    }
}
