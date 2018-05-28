package de.sciss.neuralgas.sphere

final class LocVar(var theta: Double, var phi: Double) extends Loc {
  val cosTheta: Double = Math.cos(theta)  // lazy?
  val sinTheta: Double = Math.sin(theta)  // lazy?
}