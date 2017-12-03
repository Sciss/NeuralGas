package de.sciss.demogng

import java.awt.event.{InputEvent, KeyEvent}
import java.awt.{Dimension, Graphics2D}
import java.io.File
import javax.imageio.ImageIO
import javax.swing.{Box, BoxLayout, JPanel, KeyStroke}

import de.sciss.pdflitz.Generate.Source
import de.sciss.pdflitz.SaveAction

import scala.swing.Swing

object MainPDF {
  final class JavaAWT(peer: java.awt.Component) extends Source {
    def render(g: Graphics2D): Unit = peer.paint(g)
    def size: Dimension = peer.getSize
    def preferredSize: Dimension = peer.getPreferredSize
  }

  def main(args: Array[String]): Unit = {
    val img = if (args.length >= 2 && args(0) == "--image") ImageIO.read(new File(args(1))) else null
    val invert = args.contains("--invert")
    Swing.onEDT {
      val main    = new Main(img, invert)
      main.run()
      val source  = new JavaAWT(main.getDemo.getPanel)
      val f = main.getFrame
      val a = SaveAction(source :: Nil)
      a.accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK))
      a.setupMenu(f)
      // hack to get menu to show even with heavy weight content pane
      val cpOrig = f.getContentPane
      f.setContentPane(new JPanel {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
        add(Box.createVerticalStrut(32))
        add(cpOrig)
      })
      f.revalidate()
      f.pack()

      val c = main.getDemo.getComputation.asInstanceOf[ImageComputeGNG]
      println(s"num-dots: ${c.getNumDots}")
    }
  }
}
