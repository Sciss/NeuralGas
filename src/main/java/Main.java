import javax.swing.*;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.*;
import java.net.URL;

public class Main implements Runnable, AppletStub {
    private Main() {}

    public static void main(String[] args) {
        EventQueue.invokeLater(new Main());
    }

    public void run() {
        final JFrame f = new JFrame("Demo GNG");
        final JApplet applet = new DemoGNG();
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

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public URL getDocumentBase() {
        return null;
    }

    @Override
    public URL getCodeBase() {
        return null;
    }

    @Override
    public String getParameter(String name) {
        return null;
    }

    @Override
    public AppletContext getAppletContext() {
        return null;
    }

    @Override
    public void appletResize(int width, int height) {

    }
}