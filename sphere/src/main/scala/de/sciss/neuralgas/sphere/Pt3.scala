/*
 *  Pt3.scala
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

final case class Pt3(x: Double, y: Double, z: Double) {
  override def toString = f"$productPrefix($x%g, $y%g, $z%g)"

  import Math._

  def length: Double = sqrt(x*x + y*y + z*z)

  def * (d: Double): Pt3 = Pt3(x * d, y * d, z * d)

  def + (that: Pt3): Pt3 = Pt3(this.x + that.x, this.y + that.y, this.z + that.z)

  def normalized: Pt3 = this * (1.0/length)

  def dot(that: Pt3): Double = this.x * that.x + this.y * that.y + this.z * that.z

  def distance(that: Pt3): Double = sqrt(distanceSq(that))

  def distanceSq(that: Pt3): Double = {
    val dx = this.x - that.x
    val dy = this.y - that.y
    val dz = this.z - that.z
    dx*dx + dy*dy + dz*dz
  }

  def cross(that: Pt3): Pt3 = {
    val xOut = this.y * that.z - this.z * that.y
    val yOut = this.z * that.x - this.x * that.z
    val zOut = this.x * that.y - this.y * that.x
    Pt3(xOut, yOut, zOut)
  }

  def normalizedCross (that: Pt3): Pt3 = (this cross that).normalized

  def toPolar: Polar = {
    val theta = acos(z)
    val phi   = atan2(y, x)
    Polar(theta, phi)
  }

//  def toLatLon: LatLon = {
//    val theta = acos(z)
//    val phi   = atan2(y, x)
//    val lat   = PiH - theta
//    val lon   = phi
//    LatLon(lat, lon)
//  }

  def rotateX(a: Double): Pt3 = {
    val cosA  = cos(a)
    val sinA  = sin(a)
    val yR    = y*cosA - z*sinA
    val zR    = y*sinA + z*cosA
    copy(y = yR, z = zR)
  }

  def rotateY(a: Double): Pt3 = {
    val cosA  = cos(a)
    val sinA  = sin(a)
    val xR    = x*cosA - z*sinA
    val zR    = x*sinA + z*cosA
    copy(x = xR, z = zR)
  }

  def rotateZ(a: Double): Pt3 = {
    val cosA  = cos(a)
    val sinA  = sin(a)
    val xR    = x*cosA - y*sinA
    val yR    = x*sinA + y*cosA
    copy(x = xR, y = yR)
  }
}
