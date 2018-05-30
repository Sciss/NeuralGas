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

      loc.phi   = rnd.nextInt(2) * math.Pi // 0.0 // 0.0 // rnd.nextInt(4) * math.Pi / 4 //  rnd.nextDouble() * math.Pi
      loc.theta = rnd.nextDouble() * math.Pi // 0.0 // rnd.nextInt(4) * math.Pi / 2 // 0.0
//
//      val p   = Polar(loc.theta, loc.phi)
//      val xyz = p.toCartesian
////      val isNorm = xyz == xyz.normalized
//      println(s"Drew $p -- $xyz")
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
      pd          = new RandomPD(2), // new RandomPD(1),
      maxEdgeAge  = 5000,
      utility     = 1000,
      beta        = 0.0005,
      epsilon     = 0.1,
      epsilon2    = 0.001,
      alpha       = 0.5,
      lambda      = 1.0/50
    )
    val sphere = SphereGNG(config)
    sphere.step()
    val t0 = System.currentTimeMillis()
    for (_ <- 0 until 40000) sphere.step()
    val t1 = System.currentTimeMillis()

//    val line = new LineStrip(intpCoords)
//    line.setWireframeColor(Color.BLACK)

    val chart = new AWTChart(Quality.Intermediate)
    val sq = sphere.nodeIterator.toList
    println(s"took ${t1-t0}ms. sq.size = ${sq.size}") // 7.3s

    def mkCoord(in: Polar): Coord3d = {
      import in._
      val sinTheta  = sin(theta)
      val x         = sinTheta * cos(phi)
      val y         = sinTheta * sin(phi)
      val z         = cos(theta)
      new Coord3d(x, y, z)
    }

    sphere.edgeIterator.foreach { e =>
      val p1 = e.from.toPolar
      val p2 = e.to  .toPolar

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
        mkCoord(p)
      }

//      val ln = new LineStrip(c1, c2)
      val ln = new LineStrip(c: _*)
      ln.setWireframeColor(Color.BLACK)
      chart.add(ln)
    }

//    val loc = new LocVar
//    val sq1 = Vector.fill(100) { config.pd.poll(loc); Polar(loc.theta, loc.phi) }
//
//    sq1.foreach { p =>
//      val c = mkCoord(p)
//      chart.add(new Point(c, Color.BLUE, 5f))
//    }

    sq.foreach { n =>
      val p = n.toPolar
      val c = mkCoord(p)
      chart.add(new Point(c, Color.RED, 5f))
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
