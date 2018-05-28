package de.sciss.neuralgas.sphere

import org.jzy3d.chart.{AWTChart, ChartLauncher}
import org.jzy3d.colors.Color
import org.jzy3d.maths.{Coord3d, Scale}
import org.jzy3d.plot3d.primitives.Point
import org.jzy3d.plot3d.rendering.canvas.Quality

import scala.math.{cos, sin}
import scala.swing.Swing

object SphereDemo {
  object RandomPD extends PD {
    def poll(loc: LocVar): Unit = {
      loc.theta = math.random() * math.Pi
      loc.phi   = math.random() * math.Pi * 2
    }
  }

  def main(args: Array[String]): Unit = {
    Swing.onEDT(run())
  }

  def run(): Unit = {
    val config = SphereGNG.Config(pd = RandomPD, stepSize = 1000)
    val sphere = SphereGNG(config)
    sphere.step()

//    val line = new LineStrip(intpCoords)
//    line.setWireframeColor(Color.BLACK)

    val chart = new AWTChart(Quality.Intermediate)
    val sq = sphere.nodeIterator.toList
    println(s"sq.size = ${sq.size}")
    sq.foreach { p =>
      import p._
      val sinTheta  = sin(theta)
      val x         = sinTheta * cos(phi)
      val y         = sinTheta * sin(phi)
      val z         = cos(theta)
      val c         = new Coord3d(x, y, z)
      chart.add(new Point(c, Color.BLUE, 3f))
    }

//    chart.add(line)
//    chart.add(ctlPts .asJava)
//    chart.add(intpPts.asJava)
//    chart

//    val factory = AWTChartComponentFactory
//    val mouse = ChartLauncher.configureControllers(chart, "Sphere", true, false)
//    chart.render()
//    val f = new FrameAWT(chart, new Rectangle(0, 0, 800, 600), "Sphere")

    // chart.setScale(new Scale(-1, +1))

    ChartLauncher.openChart(chart)
//
//    new MainFrame {
//
//      contents = chart
//    }
  }
}
