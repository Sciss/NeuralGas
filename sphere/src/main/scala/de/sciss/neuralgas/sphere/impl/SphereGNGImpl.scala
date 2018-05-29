/*
 *  SphereGNGImpl.scala
 *  (NeuralGas)
 *
 *  Copyright (c) 2018 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package de.sciss.neuralgas.sphere
package impl

import de.sciss.neuralgas.sphere.SphereGNG.{Config, Edge, Node}

object SphereGNGImpl {
  def apply(config: Config): SphereGNG = {
    val res = new Impl(config)
    res.init()
    res
  }

  private final class Impl(val config: Config) extends SphereGNG {
    private[this] val loc         = new LocVar

    private[this] val nodes       = new Array[Node](config.maxNodes0)
    private[this] val edges       = new Array[Edge](config.maxEdges0)
    private[this] var numNodes    = 0
    private[this] var numEdges    = 0
//    private[this] var stepCount   = 0
    private[this] val decay       = 1.0 - config.beta
    private[this] val rnd         = new util.Random(config.seed)
    private[this] var _maxNodes   = config.maxNodes0
    private[this] var nodeIdCount = 0

    def maxNodes: Int = _maxNodes

    def maxNodes_=(value: Int): Unit =
      _maxNodes = value

    def init(): Unit = {
      nodes(0) = mkRandomNode()
      nodes(1) = mkRandomNode()
      numNodes = 2
      addEdge(0, 1)
      checkConsistency()
    }

    private def mkNode(): Node = {
      val id = nodeIdCount
      nodeIdCount += 1
      new Node(id = id, maxNeighbors = config.maxNeighbors)
    }

    private def mkRandomNode(): Node = {
      val res   = mkNode()
      config.pd.poll(loc)
      res.updateTri(loc.theta, loc.phi)

//      require (!hasNaNs(res), "Oh noes 4")

      res
    }

    def step(): Unit = {
      import config._
      // stepCount += 1

      var maxError        = 0.0
      var maxErrorIdx     = 0
      var minUtility      = Double.PositiveInfinity
      var minUtilityIdx   = 0
      var minDist         = Double.PositiveInfinity
      var minDistIdx      = 0
      var nextMinDist     = Double.PositiveInfinity
      var nextMinDistIdx  = 0
      var toDelete        = -1

      pd.poll(loc)
      loc.updateTri()

      var i = 0
      while (i < numNodes) {
        val n = nodes(i)

        // Mark a node without neighbors for deletion
        if (n.numNeighbors == 0) toDelete = i

        // val d = centralAngle(n.theta, n.phi, loc.theta, loc.phi)
        val d = centralAngle(n, loc)
        n.distance  = d
        n.error    *= decay
        n.utility  *= decay

        if (d < minDist) {
          nextMinDist     = minDist
          nextMinDistIdx  = minDistIdx
          minDist         = d
          minDistIdx      = i
        } else if (d < nextMinDist) {
          nextMinDist     = d
          nextMinDistIdx  = i
        }

        if (n.error > maxError) {
          maxError        = n.error
          maxErrorIdx     = i
        }

        if (n.utility < minUtility) {
          minUtility      = n.utility
          minUtilityIdx   = i
        }

        i += 1
      }

      val winner = nodes(minDistIdx)
      adaptNode(n = winner, n1 = winner, n2 = loc, d = winner.distance, f = epsilon)

//      if (hasNaNs(winner)) {
//        println("Oh noes 1")
//      }

      winner.error    += minDist
      winner.utility  += nextMinDist - minDist

      val numNb = winner.numNeighbors
      i = 0
      while (i < numNb) {
        val nn = winner.neighbor(i)
        val nb = nodes(nn)
        assert(nb != null)
        adaptNode(n = nb, n1 = nb, n2 = loc, d = nb.distance, f = epsilon2)

//        if (hasNaNs(nb)) {
//          println("Oh noes 2")
//        }

        i += 1
      }

      // Connect two winning nodes
      if (minDistIdx != nextMinDistIdx) addEdge(minDistIdx, nextMinDistIdx)

      // Calculate the age of the connected edges and delete too old edges
      ageEdgesOfNode(minDistIdx)

      checkConsistency()

      // Insert and delete nodes
      if (rnd.nextDouble() < lambda && numNodes < _maxNodes) {
        insertNodeBetween(maxErrorIdx, maxErrorNeighbor(maxErrorIdx))
        checkConsistency()
      }

      if ((numNodes > 2) && (numNodes > _maxNodes || maxError > minUtility * utility)) {
        deleteNode(minUtilityIdx)
        checkConsistency()
      }

      checkConsistency()

      // step += 1
    }

//    private def hasNaNs(n: Loc): Boolean =
//      n.theta.isNaN || n.phi.isNaN

    @inline
    private def checkConsistency(): Unit = ()

//    private def checkConsistency(): Unit = {
//      require (numNodes >= 0 && numEdges >= 0)
//      for (ni <- 0 until numNodes) {
//        val n = nodes(ni)
//        require (n != null)
//        for (j <- 0 until n.numNeighbors) {
//          val nj = n.neighbor(j)
//          require (nj >= 0 && nj < numNodes)
//          val m = nodes(nj)
//          require (m.isNeighbor(ni))
//
//          val ei = findEdge(ni, nj)
//          require (ei >= 0 && ei < numEdges)
//        }
//      }
//    }

    def nodeIterator: Iterator[Polar]           = nodes.iterator.map(_.toPolar).take(numNodes)
    def edgeIterator: Iterator[(Polar, Polar)]  = edges.iterator.map { e =>
      nodes(e.from).toPolar -> nodes(e.to).toPolar
    } .take(numEdges)

    private def maxErrorNeighbor(ni: Int): Int = {
      var resErr  = Double.NegativeInfinity
      var resIdx  = ni // -1
      val n       = nodes(ni)
      val nNb     = n.numNeighbors
      var i       = 0
      while (i < nNb) {
        val nn  = n.neighbor(i)
        val nb = nodes(nn)
        if (nb.error > resErr) {
          resErr = nb.error
          resIdx = nn
        }
        i += 1
      }

      resIdx
    }

    private def addEdge(from: Int, to: Int): Unit =
      if (nodes(from).isNeighbor(to)) {
        val i = findEdge(from, to)
        if (i != -1) edges(i).age = 0

      } else if (numEdges < config.maxEdges0) {
        val nFrom = nodes(from)
        val nTo   = nodes(to  )

        if (nFrom.canAddNeighbor && nTo.canAddNeighbor) {
          nFrom.addNeighbor(to  )
          nTo  .addNeighbor(from)

          val e   = new Edge(from, to)
          e.from  = from
          e.to    = to
          edges(numEdges) = e
          numEdges += 1
        }
      }

    private def insertNodeBetween(ni1: Int, ni2: Int): Unit = {
      val n   = mkNode()
      val n1  = nodes(ni1)
      val n2  = nodes(ni2)

      val alphaDecay = 1.0 - config.alpha
      n1.error *= alphaDecay
      n2.error *= alphaDecay

      // interpolate data
      n.error   = (n1.error   + n2.error  ) / 2.0
      n.utility = (n1.utility + n2.utility) / 2.0
      val d     = centralAngle(n1, n2)
      adaptNode(n = n, n1 = n1, n2 = n2, d = d, f = 0.5)

//      if (hasNaNs(n)) {
//        println("Oh noes 3")
//      }

      val numOld = numNodes
      nodes(numOld) = n
      deleteEdgeBetween(ni1, ni2)
      addEdge(ni1, numOld)
      addEdge(ni2, numOld)
      numNodes = numOld + 1
    }

    private def findEdge(i: Int, j: Int): Int = {
      var ni = 0
      val e  = edges
      val n  = numEdges
      while (ni < n) { // XXX TODO: not efficient
        val ei = e(ni)
        if ((ei.from == i && ei.to == j) ||
            (ei.from == j && ei.to == i))
          return ni

        ni += 1
      }
      -1
    }

    private def ageEdgesOfNode(ni: Int): Unit = {
      var i = numEdges - 1
      while (i >= 0) { // XXX TODO: not efficient
        val ei = edges(i)
        if (ei.from == ni || ei.to == ni) {
          ei.age += 1
          if (ei.age > config.maxEdgeAge) {
            deleteEdge(i)
          }
        }
        i -= 1
      }
    }

    private def deleteNode(ni: Int): Unit = {
      val n   = nodes(ni)
      val nNb = n.numNeighbors

      var i = 0
      while (i < nNb) {
        deleteEdgeBetween(ni, n.neighbor(0))
        i += 1
      }

      val numNew    = numNodes - 1
      numNodes      = numNew
      nodes(ni)     = nodes(numNew)
      nodes(numNew) = null

      i = 0
      while (i < numNew) { // XXX TODO: not efficient
        nodes(i).replaceNeighbor(numNew, ni)
        i += 1
      }
      i = 0
      val _numEdges = numEdges
      while (i < _numEdges) { // XXX TODO: not efficient
        edges(i).replace(numNew, ni)
        i += 1
      }
    }

    private def deleteEdgeBetween(from: Int, to: Int): Unit = {
      val i = findEdge(from, to)
      if (i != -1) {
        nodes(edges(i).from).removeNeighbor(edges(i).to   )
        nodes(edges(i).to  ).removeNeighbor(edges(i).from )
        val numNew    = numEdges - 1
        numEdges      = numNew
        edges(i)      = edges(numNew)
        edges(numNew) = null
      }
    }

    private def deleteEdge(ei: Int): Unit = {
      val e = edges(ei)
      nodes(e.from).removeNeighbor(e.to)
      nodes(e.to  ).removeNeighbor(e.from)
      val newNum    = numEdges - 1
      numEdges      = newNum
      edges(ei)     = edges(newNum)
      edges(newNum) = null
    }

    private[this] final val PiH = math.Pi * 0.5

    // cf. https://math.stackexchange.com/questions/2799079/interpolating-two-spherical-coordinates-theta-phi/
    // N.B. this is actually slightly slower than the 'avform' version below based
    // on lat/lon
    private def adaptNode_VERSION(n: Node, n1: Loc, n2: Loc, d: Double, f: Double): Unit = {
      import Math._
      val x1      = n1.sinTheta * cos(n1.phi)
      val y1      = n1.sinTheta * sin(n1.phi)
      val z1      = n1.cosTheta

      val x2      = n2.sinTheta * cos(n2.phi)
      val y2      = n2.sinTheta * sin(n2.phi)
      val z2      = n2.cosTheta

      val kx0     = y1 * z2 - z1 * y2
      val ky0     = z1 * x2 - x1 * z2
      val kz0     = x1 * y2 - y1 * x2
      val k0l     = sqrt(kx0*kx0 + ky0*ky0 + kz0*kz0)

      val kx      = kx0 / k0l
      val ky      = ky0 / k0l
      val kz      = kz0 / k0l

      val ang     = acos(x1 * x2 + y1 * y2 + z1 * z2) // == d

      val psi     = ang * f
      val cosPsi  = cos(psi)
      val sinPsi  = sin(psi)
      val cosPsiI = 1.0 - cosPsi

      val k1d     = kx * x1 + ky * y1 + kz * z1
      val k1cx    = ky * z1 - kz * y1
      val k1cy    = kz * x1 - kx * z1
      val k1cz    = kx * y1 - ky * x1

      val psiX    = x1 * cosPsi + k1cx * sinPsi + kx * k1d * cosPsiI
      val psiY    = y1 * cosPsi + k1cy * sinPsi + ky * k1d * cosPsiI
      val psiZ    = z1 * cosPsi + k1cz * sinPsi + kz * k1d * cosPsiI

      val theta   = acos(psiZ)
      val phi     = atan2(psiY, psiX)
      n.updateTri(theta, phi)
    }

    // cf. http://edwilliams.org/avform.htm
    private def adaptNode(n: Node, n1: Loc, n2: Loc, d: Double, f: Double): Unit = {
      import Math._
//      val lat1      = PiH - n1.theta
      val lon1      = n1.phi
//      val lat2      = PiH - n2.theta
      val lon2      = n2.phi

      val sinD      = sin(d)
      val a         = sin((1 - f) * d) / sinD
      val b         = if (f == 0.5) a else sin(f * d) / sinD

//      val cosLat1 = cos(lat1)
//      assert(abs(cosLat1 - n1.sinTheta) < 1.0e-4, s"$cosLat1 versus ${n1.sinTheta}")
      val cosLat1   = n1.sinTheta
      val cosLon1   = cos(lon1)
//      val cosLat2 = cos(lat2)
//      assert(abs(cosLat2 - n2.sinTheta) < 1.0e-4, s"$cosLat2 versus ${n2.sinTheta}")
      val cosLat2   = n2.sinTheta
      val cosLon2   = cos(lon2)
//      val sinLat1 = sin(lat1)
//      assert(abs(sinLat1 - n1.cosTheta) < 1.0e-4, s"$sinLat1 versus ${n1.cosTheta}")
      val sinLat1   = n1.cosTheta
      val sinLon1   = sin(lon1)
//      val sinLat2 = sin(lat2)
//      assert(abs(sinLat2 - n2.cosTheta) < 1.0e-4, s"$sinLat2 versus ${n2.cosTheta}")
      val sinLat2   = n2.cosTheta
      val sinLon2   = sin(lon2)
      val aCosLat1  = a * cosLat1
      val bCosLat2  = b * cosLat2
      val x         = aCosLat1 * cosLon1 + bCosLat2 * cosLon2
      val y         = aCosLat1 * sinLon1 + bCosLat2 * sinLon2
      val z         = a * sinLat1        + b * sinLat2
      val lat       = atan2(z, sqrt(x * x + y * y))
      val lon       = atan2(y, x)

      val theta     = PiH - lat
      val phi       = lon
      n.updateTri(theta, phi)
    }

    @inline
    private def centralAngle(n1: Loc, n2: Loc): Double = {
      import Math._
      acos(n1.cosTheta * n2.cosTheta + n1.sinTheta * n2.sinTheta * cos(n1.phi - n2.phi))
    }
  }
}
