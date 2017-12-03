package de.sciss.demogng;

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
    private final boolean       hasImage;

    private JFrame f;
    private DemoGNG applet;

    public Main(BufferedImage img, boolean invert) {
        this.img    = img;
        hasImage    = img != null;
        imgInvert   = invert;
    }

    public static void main(String[] args) throws IOException {
        final BufferedImage img;

        if (args.length >= 2 && args[0].equals("--image")) {
            img = ImageIO.read(new File(args[1]));
        } else {
            img = null;
        }
        boolean invert = false;
        for (String arg : args) {
            if (arg.equals("--invert")) {
                invert = true;
                break;
            }
        }
        EventQueue.invokeLater(new Main(img, invert));
    }

    public JFrame  getFrame() { return f     ; }
    public DemoGNG getDemo () { return applet; }

    public void run() {
        f       = new JFrame("Demo GNG");
        applet  = hasImage ? new ImageDemoGNG(img, imgInvert) : new DemoGNG();
        f.getContentPane().add(applet);
        f.setMinimumSize(new Dimension(768, 768));
        f.pack();
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
        applet.setStub(this);
        applet.init();
        applet.start();
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