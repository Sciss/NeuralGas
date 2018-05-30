/*
 *  Observer.scala
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

object Observer {
  object Dummy extends Observer {
    def gngNodeUpdated  (n: Node): Unit = ()
    def gngEdgeUpdated  (e: Edge): Unit = ()
    def gngNodeInserted (n: Node): Unit = ()
    def gngEdgeInserted (e: Edge): Unit = ()
    def gngNodeRemoved  (n: Node): Unit = ()
    def gngEdgeRemoved  (e: Edge): Unit = ()
  }
}
trait Observer {
  def gngNodeUpdated  (n: Node): Unit
  def gngEdgeUpdated  (e: Edge): Unit
  def gngNodeInserted (n: Node): Unit
  def gngEdgeInserted (e: Edge): Unit
  def gngNodeRemoved  (n: Node): Unit
  def gngEdgeRemoved  (e: Edge): Unit
}
