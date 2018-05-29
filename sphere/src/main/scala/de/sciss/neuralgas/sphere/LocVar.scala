package de.sciss.neuralgas.sphere

final class LocVar extends Loc {
  var theta   : Double = 0.0
  var phi     : Double = 0.0
  var cosTheta: Double = 1.0
  var sinTheta: Double = 0.0

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