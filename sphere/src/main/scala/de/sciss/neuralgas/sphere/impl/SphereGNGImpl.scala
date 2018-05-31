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

import java.io.{BufferedInputStream, BufferedOutputStream, DataInputStream, DataOutputStream, File, FileInputStream, FileOutputStream, InputStream, OutputStream}

import de.sciss.neuralgas.sphere.SphereGNG.Config

import scala.collection.mutable

object SphereGNGImpl {
  def apply(config: Config): SphereGNG = {
    val res = new Impl(config)
    res.init()
    res
  }

  private final val SPHERE_COOKIE = 0x53474E47 // "SGNG"

//  private final val CONFIG_COOKIE = 0x53474366 // "SGCf"

//  def saveConfig(f: File, c: Config): Unit = {
//  }

//  def loadConfig(f: File): Config = {
//  }

  private final class Impl(val config: Config) extends SphereGNG {
    private[this] val loc         = new LocVarImpl

    // persisted
    private[this] var nodeIdCount = 0
    private[this] var numNodes    = 0
    private[this] val nodes       = new Array[NodeImpl](config.maxNodes0)
    private[this] val edgeMap     = mutable.Map.empty[Int, mutable.Buffer[EdgeImpl]]

    // currently not persisted
    private[this] val decay       = 1.0 - config.beta
    private[this] val rnd         = new util.Random(config.seed)
    private[this] var _maxNodes   = config.maxNodes0

    def maxNodes: Int = _maxNodes

    def maxNodes_=(value: Int): Unit =
      _maxNodes = value

    def init(): Unit = {
      val n1 = mkRandomNode()
      val n2 = mkRandomNode()
      nodes(0) = n1
      nodes(1) = n2
      numNodes = 2
      import config._
      observer.gngNodeInserted(n1)
      observer.gngNodeInserted(n2)
      addEdgeAndFire(n1, n2)
//      checkConsistency()
    }

    def saveState(f: File): Unit = {
      val fOut = new FileOutputStream(f)
      try {
        writeState(fOut)
      } finally {
        fOut.close()
      }
    }

    def loadState(f: File): Unit = {
      val fIn = new FileInputStream(f)
      try {
        readState(fIn)
      } finally {
        fIn.close()
      }
    }

    def writeState(out: OutputStream): Unit = {
      val dOut = new DataOutputStream(new BufferedOutputStream(out))
      import dOut._
      writeInt(SPHERE_COOKIE)
      writeInt(nodeIdCount)
      writeInt(numNodes)
      var i = 0
      while (i < numNodes) {
        val n = nodes(i)
        writeInt    (n.id     )
        writeDouble (n.theta  )
        writeDouble (n.phi    )
        writeDouble (n.utility)
        writeDouble (n.error  )
        writeShort  (n.numNeighbors)
        var j = 0
        while (j < n.numNeighbors) {
          val nb = n.neighbor(j)
          writeInt(nb.id)
          j += 1
        }
        i += 1
      }

      writeInt(edgeMap.size)
      edgeMap.foreach { case (id, buf) =>
        writeInt(id)
        writeInt(buf.size)
        buf.foreach { e =>
          writeInt(e.from.id)
          writeInt(e.to  .id)
          writeInt(e.age    )
        }
      }

      flush()
    }

    def readState(in: InputStream): Unit = {
      val dIn = new DataInputStream(new BufferedInputStream(in))
      import dIn._
      val cookie = readInt()
      require (cookie == SPHERE_COOKIE,
        s"Unexpected cookie, found ${cookie.toHexString} instead of ${SPHERE_COOKIE.toHexString}")
      nodeIdCount = readInt()
      numNodes    = readInt()

      val neighborData  = new Array[Array[Int]](numNodes)
      val nodeMap       = mutable.Map.empty[Int, NodeImpl]

      var i = 0
      while (i < numNodes) {
        val id = readInt()
        val n = new NodeImpl(id = id, maxNeighbors = config.maxNeighbors)
        n.theta   = readDouble()
        n.phi     = readDouble()
        n.utility = readDouble()
        n.error   = readDouble()
        nodes(i)  = n
        nodeMap += n.id -> n
        val numNeighbors = readShort()
        val neighbors = new Array[Int](numNeighbors)
        neighborData(i) = neighbors
        var j = 0
        while (j < numNeighbors) {
          val nbId = readInt()
          neighbors(j) = nbId
          j += 1
        }
        i += 1
      }

      // resolve neighbours
      i = 0
      while (i < numNodes) {
        val neighbors = neighborData(i)
        val n         = nodes(i)
        var j = 0
        while (j < neighbors.length) {
          val nbId  = neighbors(j)
          val nb    = nodeMap(nbId)
          n.addNeighbor(nb)
          j += 1
        }
        i += 1
      }

      val edgeMapSz = readInt()
      i = 0
      edgeMap.clear()
      while (i < edgeMapSz) {
        val key = readInt()
        val numEdges = readInt()
        val buf = mutable.Buffer.empty[EdgeImpl]
        var j = 0
        while (j < numEdges) {
          val fromId  = readInt()
          val toId    = readInt()
          val age     = readInt()
          val from    = nodeMap(fromId)
          val to      = nodeMap(toId  )
          val e       = new EdgeImpl(from, to)
          e.age       = age
          buf += e
          j += 1
        }
        edgeMap += key -> buf
        i += 1
      }
    }

    private def mkNode(): NodeImpl = {
      val id = nodeIdCount
      nodeIdCount += 1
      new NodeImpl(id = id, maxNeighbors = config.maxNeighbors)
    }

    private def mkRandomNode(): NodeImpl = {
      val res   = mkNode()
      config.pd.poll(loc)
      res.updateTri(loc.theta, loc.phi)
      res
    }

    def step(): Unit = {
      import config._
      // stepCount += 1

      var maxError        = 0.0
      var maxErrorN       = null : NodeImpl
      var minUtility      = Double.PositiveInfinity
      var minUtilityIdx   = 0
      var minDist         = Double.PositiveInfinity
      var minDistN        = null : NodeImpl
      var nextMinDist     = Double.PositiveInfinity
      var nextMinDistN    = null : NodeImpl
      var toDelete        = -1

      pd.poll(loc)
      loc.updateTri()

      var i = 0
      while (i < numNodes) {
        val n = nodes(i)

        // Mark a node without neighbors for deletion
        if (n.numNeighbors == 0) toDelete = i

        val d = centralAngle(n, loc)
        n.distance  = d
        n.error    *= decay
        n.utility  *= decay

        if (d < minDist) {
          nextMinDist   = minDist
          nextMinDistN  = minDistN
          minDist       = d
          minDistN      = n
        } else if (d < nextMinDist) {
          nextMinDist   = d
          nextMinDistN  = n
        }

        if (n.error > maxError) {
          maxError      = n.error
          maxErrorN     = n
        }

        if (n.utility < minUtility) {
          minUtility    = n.utility
//          minUtilityN   = n
          minUtilityIdx = i
        }

        i += 1
      }

      val winner = minDistN // nodes(minDistIdx)
      adaptNode(n = winner, n1 = winner, n2 = loc, d = winner.distance, f = epsilon)
      winner.error    += minDist
      winner.utility  += nextMinDist - minDist
      observer.gngNodeUpdated(winner)

      val numNb = winner.numNeighbors
      i = 0
      while (i < numNb) {
        val nb = winner.neighbor(i)
        assert(nb != null)
        adaptNode(n = nb, n1 = nb, n2 = loc, d = nb.distance, f = epsilon2)
        observer.gngNodeUpdated(nb)
        i += 1
      }

      // Connect two winning nodes
      if (minDistN != nextMinDistN) addEdgeAndFire(minDistN, nextMinDistN)

      // Calculate the age of the connected edges and delete too old edges
      ageEdgesOfNodeAndFire(minDistN)

//      checkConsistency()

      // Insert and delete nodes
      if (rnd.nextDouble() < lambda && numNodes < _maxNodes) {
        insertNodeBetweenAndFire(maxErrorN, maxErrorNeighbor(maxErrorN))
//        checkConsistency()
      }

      if ((numNodes > 2) && (numNodes > _maxNodes || maxError > minUtility * utility)) {
//        deleteNode(minUtilityN)
        deleteNodeAndFire(minUtilityIdx)
//        checkConsistency()
      }

//      checkConsistency()

      // step += 1
    }

//    private def hasNaNs(n: Loc): Boolean =
//      n.theta.isNaN || n.phi.isNaN

//    @inline
//    private def checkConsistency(): Unit = ()

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

    def nodeIterator: Iterator[Node] = nodes.iterator.take(numNodes)
    def edgeIterator: Iterator[Edge] = edgeMap.valuesIterator.flatMap { buf =>
      buf.iterator.collect {
        case e if e.from.id < e.to.id => e
      }
    }

    private def maxErrorNeighbor(n: NodeImpl): NodeImpl = {
      var resErr  = Double.NegativeInfinity
      var res     = n
      val nNb     = n.numNeighbors
      var i       = 0
      while (i < nNb) {
        val nb = n.neighbor(i)
        if (nb.error > resErr) {
          resErr  = nb.error
          res     = nb
        }
        i += 1
      }

      res
    }

    private def addEdgeAndFire(from: NodeImpl, to: NodeImpl): Unit =
      if (from.isNeighbor(to)) {
        val fromId  = from.id
        val buf     = edgeMap(fromId)
        val e       = buf.find(e => e.from.id == fromId || e.to.id == fromId).get
        if (e.age != 0) {
          e.age = 0
          config.observer.gngEdgeUpdated(e)
        }

      } else {
        if (from.canAddNeighbor && to.canAddNeighbor) {
          from.addNeighbor(to  )
          to  .addNeighbor(from)

          val bufFrom = edgeMap.getOrElseUpdate(from.id, mutable.Buffer.empty)
          val bufTo   = edgeMap.getOrElseUpdate(to  .id, mutable.Buffer.empty)
          val e       = new EdgeImpl(from, to)
          bufFrom += e
          bufTo   += e

          config.observer.gngEdgeInserted(e)
        }
      }

    private def insertNodeBetweenAndFire(n1: NodeImpl, n2: NodeImpl): Unit = {
      val n   = mkNode()

      val alphaDecay = 1.0 - config.alpha
      n1.error *= alphaDecay
      n2.error *= alphaDecay

      // interpolate data
      n.error   = (n1.error   + n2.error  ) / 2.0
      n.utility = (n1.utility + n2.utility) / 2.0
      val d     = centralAngle(n1, n2)
      adaptNode(n = n, n1 = n1, n2 = n2, d = d, f = 0.5)

      deleteEdgeBetweenAndFire(n1, n2)

      val numOld = numNodes
      nodes(numOld) = n
      numNodes = numOld + 1
      config.observer.gngNodeInserted(n)
      addEdgeAndFire(n1, n)
      addEdgeAndFire(n2, n)
    }

    private def ageEdgesOfNodeAndFire(n: Node): Unit = {
      val edges = edgeMap(n.id)
      edges.foreach { e =>
        e.age += 1
        if (e.age <= config.maxEdgeAge) {
          config.observer.gngEdgeUpdated(e)
        } else{
          deleteEdgeAndFire(e)
        }
      }
    }

    private def deleteNodeAndFire(ni: Int): Unit = {
      val n     = nodes(ni)
      val nNb   = n.numNeighbors

      var i = 0
      while (i < nNb) {
        deleteEdgeBetweenAndFire(n, n.neighbor(0))
        i += 1
      }

      val numNew    = numNodes - 1
      numNodes      = numNew
      nodes(ni)     = nodes(numNew)
      nodes(numNew) = null

      edgeMap.remove(n.id)

      config.observer.gngNodeRemoved(n)
    }

    @inline
    private def remove[A](xs: mutable.Buffer[A], elem: A): Unit =
      xs.remove(xs.indexOf(elem))

//    private def remove[A](xs: List[A], elem: A): List[A] = {
//
//      @tailrec def loop(rem: List[A], res: List[A]): List[A] = rem match {
//        case `elem` :: tail => res.reverse ::: tail
//        case hd     :: tail => loop(tail, hd :: res)
//        case Nil            => res.reverse
//      }
//
//      loop(xs, Nil)
//    }

    // takes care of removing neighbours as well
    private def deleteEdgeAndFire(e: EdgeImpl): Unit = {
      import e._
      from.removeNeighbor(to)
      to  .removeNeighbor(from)
      val fromId  = from.id
      val toId    = to  .id
      remove(edgeMap(fromId), e)
      remove(edgeMap(toId  ), e)
      config.observer.gngEdgeRemoved(e)
    }

    // takes care of removing neighbours as well.
    // allowed to call this when there _is_ no edge.
    private def deleteEdgeBetweenAndFire(from: Node, to: Node): Unit = {
      val fromId  = from.id
      val buf     = edgeMap(from.id)
      val eOpt    = buf.find(e => e.from.id == fromId || e.to.id == fromId)
      eOpt.foreach(deleteEdgeAndFire)
    }

    private[this] final val PiH = math.Pi * 0.5

//    // cf. https://math.stackexchange.com/questions/2799079/interpolating-two-spherical-coordinates-theta-phi/
//    // N.B. this is actually slightly slower than the 'avform' version below based
//    // on lat/lon
//    private def adaptNode_VERSION(n: NodeImpl, n1: Loc, n2: Loc, d: Double, f: Double): Unit = {
//      import Math._
//      val x1      = n1.sinTheta * cos(n1.phi)
//      val y1      = n1.sinTheta * sin(n1.phi)
//      val z1      = n1.cosTheta
//
//      val x2      = n2.sinTheta * cos(n2.phi)
//      val y2      = n2.sinTheta * sin(n2.phi)
//      val z2      = n2.cosTheta
//
//      val kx0     = y1 * z2 - z1 * y2
//      val ky0     = z1 * x2 - x1 * z2
//      val kz0     = x1 * y2 - y1 * x2
//      val k0l     = sqrt(kx0*kx0 + ky0*ky0 + kz0*kz0)
//
//      val kx      = kx0 / k0l
//      val ky      = ky0 / k0l
//      val kz      = kz0 / k0l
//
//      val ang     = acos(x1 * x2 + y1 * y2 + z1 * z2) // == d
//
//      val psi     = ang * f
//      val cosPsi  = cos(psi)
//      val sinPsi  = sin(psi)
//      val cosPsiI = 1.0 - cosPsi
//
//      val k1d     = kx * x1 + ky * y1 + kz * z1
//      val k1cx    = ky * z1 - kz * y1
//      val k1cy    = kz * x1 - kx * z1
//      val k1cz    = kx * y1 - ky * x1
//
//      val psiX    = x1 * cosPsi + k1cx * sinPsi + kx * k1d * cosPsiI
//      val psiY    = y1 * cosPsi + k1cy * sinPsi + ky * k1d * cosPsiI
//      val psiZ    = z1 * cosPsi + k1cz * sinPsi + kz * k1d * cosPsiI
//
//      val theta   = acos(psiZ)
//      val phi     = atan2(psiY, psiX)
//      n.updateTri(theta, phi)
//    }

    // cf. http://edwilliams.org/avform.htm
    private def adaptNode(n: NodeImpl, n1: LocImpl, n2: LocImpl, d: Double, f: Double): Unit = {
      if (d == 0) {
        n.updateTri(n1.theta, n1.phi)
        return
      }

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
    private def centralAngle(n1: LocImpl, n2: LocImpl): Double = {
      import Math._
      acos(n1.cosTheta * n2.cosTheta + n1.sinTheta * n2.sinTheta * cos(n1.phi - n2.phi))
    }
  }
}
