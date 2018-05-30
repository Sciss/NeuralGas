/*
 *  LocVarImpl.scala
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

final class LocVarImpl extends LocVar with LocImpl {
  var theta   : Double = 0.0
  var phi     : Double = 0.0
  var cosTheta: Double = 1.0
  var sinTheta: Double = 0.0

  def toPolar = Polar(theta, phi)

  def updateTri(): Unit = {
    cosTheta    = Math.cos(theta)
    sinTheta    = Math.sin(theta)
  }

  def updateTri(theta: Double, phi: Double): Unit = {
    this.theta  = theta
    this.phi    = phi
    cosTheta    = Math.cos(theta)
    sinTheta    = Math.sin(theta)
  }

  override def toString = f"LocVar($theta%g, $phi%g)"
}