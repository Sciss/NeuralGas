/*
 *  PD.scala
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

import scala.annotation.tailrec

object PD {
  val Uniform = new Uniform(0L)
  final class Uniform(seed: Long) extends PD {
    private[this] val rnd = new util.Random(seed)

    @tailrec
    def poll(loc: LocVar): Unit = {
      import Math._

      val x = rnd.nextDouble() - 0.5
      val y = rnd.nextDouble() - 0.5
      val z = rnd.nextDouble() - 0.5
      if (x == 0.0 && y == 0.0 && z == 0.0) poll(loc)
      else {
        val len = sqrt(x*x + y*y + z*z)
        val xn    = x / len
        val yn    = y / len
        val zn    = z / len
        loc.theta = acos(zn)
        loc.phi   = atan2(yn, xn)
      }
    }
  }
}
/** Probability distribution. */
trait PD {
  def poll(loc: LocVar): Unit
}

