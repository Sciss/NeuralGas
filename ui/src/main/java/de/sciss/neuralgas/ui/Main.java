package de.sciss.neuralgas.ui;

import de.sciss.neuralgas.GrayImagePD;
import de.sciss.neuralgas.ImagePD;
import de.sciss.neuralgas.PD;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author Hanns Holger Rutz
 */
public final class Main implements Runnable, AppletStub {
    private final BufferedImage img;
    private final boolean       imgInvert;
    private final boolean       imgGray;
    private final boolean       hasImage;

    private JFrame f;
    private DemoGNG applet;

    public Main(BufferedImage img, boolean invert) {
        this(img, invert, false);
    }

    public Main(BufferedImage img, boolean invert, boolean gray) {
        this.img    = img;
        hasImage    = img != null;
        imgInvert   = invert;
        imgGray     = gray;
    }

    public static void main(String[] args) throws IOException {
        final BufferedImage img;

        int     imageIdx    = -1;
        boolean invert      = false;
        boolean gray        = false;

        for (int i = 0; i < args.length; i++) {
            final String a = args[i];
            if (a.equals("--image") && i + 1 < args.length) {
                imageIdx = i + 1;
            } else if (a.equals("--invert")) {
                invert = true;
            } else if (a.equals("--gray")) {
                gray = true;
            }
        }

        if (imageIdx >= 0) {
            img = ImageIO.read(new File(args[imageIdx]));
        } else {
            img = null;
        }

        EventQueue.invokeLater(new Main(img, invert, gray));
    }

    public JFrame  getFrame() { return f     ; }
    public DemoGNG getDemo () { return applet; }

    public void run() {
        f       = new JFrame("Demo GNG");
        applet  = new DemoGNG();
        final PD pd = hasImage ? (imgGray ? new GrayImagePD(img, imgInvert) : new ImagePD(img, imgInvert)) : PD.Rectangle;
        f.getContentPane().add(applet);
        f.setMinimumSize(new Dimension(768, 768));
        f.pack();
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
        applet.setStub(this);
        applet.init();
        applet.setDist(pd);
        // applet.start();
    }

    public boolean isActive() {
        return false;
    }

    public URL getDocumentBase() {
        return null;
    }

    public URL getCodeBase() {
        return null;
    }

    public String getParameter(String name) {
        return null;
    }

    public AppletContext getAppletContext() {
        return null;
    }

    public void appletResize(int width, int height) {}
}