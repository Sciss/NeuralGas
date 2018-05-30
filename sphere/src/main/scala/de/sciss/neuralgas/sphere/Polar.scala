/*
 *  Polar.scala
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

object Polar {
  def centralAngle(n1: Polar, n2: Polar): Double = {
    import Math._
    acos(cos(n1.theta) * cos(n2.theta) + sin(n1.theta) * sin(n2.theta) * cos(n1.phi - n2.phi))
  }

  def interpolate(n1: Polar, n2: Polar, f: Double): Polar = {
    import Math._
    val PiH     = PI * 0.5
    val d       = centralAngle(n1, n2)

    // http://edwilliams.org/avform.htm
    val lat1    = PiH - n1.theta
    val lon1    = n1.phi
    val lat2    = PiH - n2.theta
    val lon2    = n2.phi

    val sinD    = sin(d)
    val a       = sin((1 - f) * d) / sinD
    val b       = sin( f      * d) / sinD
    // todo: optimise to use cosTheta, sinTheta
    val cosLat1 = cos(lat1)
    val cosLon1 = cos(lon1)
    val cosLat2 = cos(lat2)
    val cosLon2 = cos(lon2)
    val sinLat1 = sin(lat1)
    val sinLon1 = sin(lon1)
    val sinLat2 = sin(lat2)
    val sinLon2 = sin(lon2)
    val x       = a * cosLat1 * cosLon1 + b * cosLat2 * cosLon2
    val y       = a * cosLat1 * sinLon1 + b * cosLat2 * sinLon2
    val z       = a * sinLat1           + b * sinLat2
    val lat     = atan2(z, sqrt(x * x + y * y))
    val lon     = atan2(y, x)

    val theta   = PiH - lat
    val phi     = lon

    Polar(theta = theta, phi = phi)
  }
}
final case class Polar(theta: Double, phi: Double) {
  import Math._

  def toCartesian: Pt3 = {
    val sinTheta = sin(theta)
    val x = sinTheta * cos(phi)
    val y = sinTheta * sin(phi)
    val z = cos(theta)
    Pt3(x, y, z)
  }
}