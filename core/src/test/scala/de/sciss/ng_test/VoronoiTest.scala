package de.sciss.ng_test

import de.sciss.neuralgas.{ComputeGNG, EdgeVoronoi, LineFloat2D, NodeGNG, SiteVoronoi, Voronoi}

import scala.collection.mutable
import scala.language.implicitConversions

object VoronoiTest {
  def main(args: Array[String]): Unit = {
    implicit def mkNode(p: (Int, Int)): NodeGNG = {
      val n = new NodeGNG
      n.x   = p._1
      n.y   = p._2
      n
    }

    implicit class SiteToPointInt(in: SiteVoronoi) {
      def toInt: (Int, Int) = {
        val x = math.round(in.coord.x)
        val y = math.round(in.coord.y)
        (x, y)
      }
    }

    val polyMap = mutable.Map.empty[(Int, Int), List[LineFloat2D]]
    val c       = new ComputeGNG
    c.nNodes    = 6
    c.maxNodes  = 6
    c.nodes(0)  = (1, 3)
    c.nodes(1)  = (3, 4)
    c.nodes(2)  = (4, 1)
    c.nodes(3)  = (5, 3)
    c.nodes(4)  = (7, 2)
    c.nodes(5)  = (7, 4)
    val v = new Voronoi(c) {
      override def out_ep(e: EdgeVoronoi): Boolean = {
        val hasLine = super.out_ep(e)
        if (hasLine) {
          val s1 = e.reg(0).toInt
          val s2 = e.reg(1).toInt
          val ln = lines(nLines - 1)
          polyMap += s1 -> (ln :: polyMap.getOrElse(s1, Nil))
          polyMap += s2 -> (ln :: polyMap.getOrElse(s2, Nil))
        }
        hasLine
      }
    }
    v.voronoiB = true
    v.setSize(8, 5)
    val b = v.computeVoronoi()
    assert(!b)
  }
}