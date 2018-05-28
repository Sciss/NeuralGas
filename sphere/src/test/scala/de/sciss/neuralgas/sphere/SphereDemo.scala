package de.sciss.neuralgas.sphere

import org.jzy3d.chart.{AWTChart, ChartLauncher}
import org.jzy3d.colors.Color
import org.jzy3d.maths.{Coord3d, Scale}
import org.jzy3d.plot3d.primitives.{LineStrip, Point}
import org.jzy3d.plot3d.rendering.canvas.Quality

import scala.math.{cos, sin}
import scala.swing.Swing

object SphereDemo {
  final class RandomEquatorPD(seed: Long) extends PD {
    private[this] val rnd = new util.Random(seed)

    def poll(loc: LocVar): Unit = {
//      loc.theta = 0.0
//      loc.phi   = rnd.nextDouble() * math.Pi * 2

      loc.theta = rnd.nextInt(4) * math.Pi / 4 //  rnd.nextDouble() * math.Pi
      loc.phi   = rnd.nextInt(4) * math.Pi / 2 // 0.0
    }
  }

  final class RandomPD(seed: Long) extends PD {
    import Math._

    private[this] val rnd = new util.Random(seed)

    def poll(loc: LocVar): Unit = {
      loc.theta = rnd.nextDouble() * PI
      loc.phi   = rnd.nextDouble() * PI * 2
//      loc.phi   = (rnd.nextDouble() * 2 - 1) * PI
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
      pd          = new RandomEquatorPD(1), // new RandomPD(1),
      maxEdgeAge  = 5000,
      utility     = 1000,
      beta        = 0.0005,
      epsilon     = 0.1,
      epsilon2    = 0.001,
      alpha       = 0.5,
      lambda      = 1.0/50
    )
    val sphere = SphereGNG(config)
    for (_ <- 0 until 10000) sphere.step()

//    val line = new LineStrip(intpCoords)
//    line.setWireframeColor(Color.BLACK)

    val chart = new AWTChart(Quality.Intermediate)
    val sq = sphere.nodeIterator.toList
    println(s"sq.size = ${sq.size}")

    sphere.edgeIterator.foreach { case (p1, p2) =>
//      val c1 = {
//        import p1._
//        val sinTheta  = sin(theta)
//        val x         = sinTheta * cos(phi)
//        val y         = sinTheta * sin(phi)
//        val z         = cos(theta)
//        new Coord3d(x, y, z)
//      }
//
//      val c2 = {
//        import p2._
//        val sinTheta  = sin(theta)
//        val x         = sinTheta * cos(phi)
//        val y         = sinTheta * sin(phi)
//        val z         = cos(theta)
//        new Coord3d(x, y, z)
//      }

      val numIntp = math.max(2, (Polar.centralAngle(p1, p2) * 20).toInt)
      val c = Vector.tabulate(numIntp) { i =>
        val f = i.toDouble / (numIntp - 1)
        val p = Polar.interpolate(p1, p2, f)
        import p._
        val sinTheta  = sin(theta)
        val x         = sinTheta * cos(phi)
        val y         = sinTheta * sin(phi)
        val z         = cos(theta)
        new Coord3d(x, y, z)
      }

//      val ln = new LineStrip(c1, c2)
      val ln = new LineStrip(c: _*)
      ln.setWireframeColor(Color.BLACK)
      chart.add(ln)
    }

    sq.foreach { p =>
      //      val theta = math.random() * math.Pi
      //      val phi   = math.random() * math.Pi * 2
      val c = {
        import p._
        val sinTheta  = sin(theta)
        val x         = sinTheta * cos(phi)
        val y         = sinTheta * sin(phi)
        val z         = cos(theta)
        new Coord3d(x, y, z)
      }
      chart.add(new Point(c, Color.BLUE, 5f))
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
    val view = chart.getView
    view.setScaleX(new Scale(-1, +1))
    view.setScaleY(new Scale(-1, +1))
    view.setScaleZ(new Scale(-1, +1))

    ChartLauncher.openChart(chart)

//
//    new MainFrame {
//
//      contents = chart
//    }
  }
}
