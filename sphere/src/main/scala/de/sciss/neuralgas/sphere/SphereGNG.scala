/*
 *  SphereGNG.scala
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

import de.sciss.neuralgas.sphere.SphereGNG.Config

object SphereGNG {
  final class Node(maxNeighbors: Int) extends Loc {
    private[this] var _numNeighbors = 0
    private[this] val neighbors = new Array[Int](maxNeighbors)

    var theta     = 0.0
    var phi       = 0.0
    var utility   = 0.0
    var error     = 0.0
    var distance  = 0.0

    var cosTheta  = 1.0
    var sinTheta  = 0.0

    def toPolar: Polar = Polar(theta = theta, phi = phi)

    def canAddNeighbor: Boolean = _numNeighbors < neighbors.length

    def addNeighbor(ni: Int): Unit = {
      neighbors(_numNeighbors) = ni
      _numNeighbors += 1
    }

    def removeNeighbor(ni: Int): Unit = {
      var i = 0
      while (i < _numNeighbors) {
        if (neighbors(i) == ni) {
          _numNeighbors -= 1
          neighbors(i)             = neighbors(_numNeighbors)
          neighbors(_numNeighbors) = -1
          return
        }
        i += 1
      }
      // throw new IllegalArgumentException(s"No neighbor $ni found")
    }

    def replaceNeighbor(before: Int, now: Int): Unit = {
      var i = 0
      while (i < _numNeighbors) {
        if (neighbors(i) == before) {
          neighbors(i) = now
          return
        }
        i += 1
      }
      // throw new IllegalArgumentException(s"No neighbor $before found")
    }

    def neighbor(idx: Int): Int = neighbors(idx)

    def updateTri(): Unit = {
      cosTheta = Math.cos(theta)
      sinTheta = Math.sin(theta)
    }

    def numNeighbors: Int = _numNeighbors

    def isNeighbor(ni: Int): Boolean = {
      var i = 0
      while (i < _numNeighbors) {
        if (neighbors(i) == ni) return true
        i += 1
      }
      false
    }
  }

  final class Edge(var from: Int, var to: Int) {
    var age: Int = 0

    def replace(before: Int, now: Int): Unit = {
      if      (from == before) from = now
      else if (to   == before) to   = now
      // else throw new IllegalArgumentException(s"No connecting node $before found")
    }
  }

  /** Configuration of the algorithm.
    *
    * @param pd         probability distribution function
    * @param stepSize   number of iterations to perform in each step
    * @param epsilon    adaptation factor for 'winner'
    *                   (factor of moving winner node towards pd-emitted position)
    * @param epsilon2   adaptation factor for neighbours of 'winner'
    *                   (factor of moving neighbour nodes towards pd-emitted position)
    * @param beta       controls decay of errors and utilities (decay is `1.0 - beta`)
    * @param alpha      controls decay of errors and utilities for winning nodes
    *                   (when inserting a new node between them; decay is `1.0 - alpha`)
    * @param lambda     probability of inserting a node (each step)
    * @param utility    scaling factor such that a node is deleted when
    *                   `utility * node-utility < max-error`
    * @param seed       random number generator initial seed value
    * @param maxNodes0  initial maximum number of nodes. note that when changing `maxNodes`
    *                   later, the values are always clipped to `maxNodes0` (there will never
    *                   be more than `maxNodes0` nodes).
    */
  final case class Config(
                           pd           : PD,
                           stepSize     : Int     = 10,
                           epsilon      : Double  = 0.02,
                           epsilon2     : Double  = 0.01,
                           beta         : Double  = 0.001,
                           alpha        : Double  = 0.001,
                           lambda       : Double  = 1.0/100,
                           utility      : Double  = 4.0,
                           seed         : Long    = 0L,
                           maxNodes0    : Int     = 1000,
                           maxEdges0    : Int     = 6000,
                           maxEdgeAge   : Int     = 80,
                           maxNeighbors : Int     = 10
                         )

  def apply(config: Config): SphereGNG = impl.SphereGNGImpl(config)
}

/** A variant of the Growing Neural Gas with Utility (GNG-U) algorithm that uses
  * spherical coordinates. Other changes include:
  *
  * - instead of an iteration count for 'lambda' for new node insertion, we use a probability
  */
trait SphereGNG {
  def config: Config

  def step(): Unit

  var maxNodes: Int

  def nodeIterator: Iterator[Polar]
  def edgeIterator: Iterator[(Polar, Polar)]
}