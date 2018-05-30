/*
 *  NodeImpl.scala
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

final class NodeImpl(val id: Int, maxNeighbors: Int) extends Node with LocVar with LocImpl {
  private[this] var _numNeighbors = 0
  private[this] val neighbors = new Array[NodeImpl](maxNeighbors)

  var theta     = 0.0
  var phi       = 0.0
  var utility   = 0.0
  var error     = 0.0
  var distance  = 0.0

  var cosTheta  = 1.0
  var sinTheta  = 0.0

  override def toString = f"Node($id, theta = $theta%g, phi = $phi%g); error = $error%g, utility = $utility%g, distance = $distance%g"

  def toPolar: Polar = Polar(theta = theta, phi = phi)

  def canAddNeighbor: Boolean = _numNeighbors < neighbors.length

  def addNeighbor(n: NodeImpl): Unit = {
    neighbors(_numNeighbors) = n
    _numNeighbors += 1
  }

  def removeNeighbor(n: NodeImpl): Unit = {
    val ni = n.id
    var i = 0
    while (i < _numNeighbors) {
      if (neighbors(i).id == ni) {
        _numNeighbors  -= 1
        neighbors(i)    = neighbors(_numNeighbors)
        neighbors(_numNeighbors) = null
        return
      }
      i += 1
    }
    // throw new IllegalArgumentException(s"No neighbor $ni found")
  }

  def replaceNeighbor(before: NodeImpl, now: NodeImpl): Unit = {
    val ni = before.id
    var i = 0
    while (i < _numNeighbors) {
      if (neighbors(i).id == ni) {
        neighbors(i) = now
        return
      }
      i += 1
    }
    // throw new IllegalArgumentException(s"No neighbor $before found")
  }

  def neighbor(idx: Int): NodeImpl = neighbors(idx)

  def updateTri(theta: Double, phi: Double): Unit = {
    this.theta  = theta
    this.phi    = phi
    cosTheta    = Math.cos(theta)
    sinTheta    = Math.sin(theta)
  }

  def numNeighbors: Int = _numNeighbors

  def isNeighbor(n: NodeImpl): Boolean = {
    val ni = n.id
    var i = 0
    while (i < _numNeighbors) {
      if (neighbors(i).id == ni) return true
      i += 1
    }
    false
  }
}
