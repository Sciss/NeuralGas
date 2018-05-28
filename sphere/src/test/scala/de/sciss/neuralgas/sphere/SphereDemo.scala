package de.sciss.neuralgas.sphere

import org.jzy3d.chart.{AWTChart, ChartLauncher}
import org.jzy3d.colors.Color
import org.jzy3d.maths.Coord3d
import org.jzy3d.plot3d.primitives.Point
import org.jzy3d.plot3d.rendering.canvas.Quality

import scala.annotation.tailrec
import scala.math.{cos, sin}
import scala.swing.Swing

object SphereDemo {
  final class RandomPD(seed: Long) extends PD {
    import Math._

    private[this] val rnd = new util.Random(seed)

    def poll(loc: LocVar): Unit = {
      loc.theta = rnd.nextDouble() * PI
      loc.phi   = rnd.nextDouble() * PI * 2
    }

//    @tailrec
//    def poll(loc: LocVar): Unit = {
//      val x0 = rnd.nextDouble() * 2 - 1
//      val y0 = rnd.nextDouble() * 2 - 1
//      val z0 = rnd.nextDouble() * 2 - 1
//      val l0 = sqrt(x0*x0 + y0*y0 + z0*z0)
//      if (l0 == 0) poll(loc)
//      else {
//        val x  = x0 / l0
//        val y  = y0 / l0
//        val z  = z0 / l0
//
////        println(f"[$x%g, $y%g, $z%g]")
//
//        loc.theta = acos(z)
//        loc.phi   = atan2(y, x)
//      }
//    }
  }

  def main(args: Array[String]): Unit = {
    Swing.onEDT(run())
  }

  def run(): Unit = {
    val config = SphereGNG.Config(
      pd          = new RandomPD(1),
      stepSize    = 30000,
      maxEdgeAge  = 5000,
      utility     = 1000,
      lambda      = 1.0/50
    )
    val sphere = SphereGNG(config)
    sphere.step()

//    val line = new LineStrip(intpCoords)
//    line.setWireframeColor(Color.BLACK)

    val chart = new AWTChart(Quality.Intermediate)
    val sq = sphere.nodeIterator.toList
    println(s"sq.size = ${sq.size}")
    sq.foreach { p =>
      import p._
      val theta = math.random() * math.Pi
      val phi   = math.random() * math.Pi * 2
      val c = {
        val sinTheta  = sin(theta)
        val x         = sinTheta * cos(phi)
        val y         = sinTheta * sin(phi)
        val z         = cos(theta)
        new Coord3d(x, y, z)
      }
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
