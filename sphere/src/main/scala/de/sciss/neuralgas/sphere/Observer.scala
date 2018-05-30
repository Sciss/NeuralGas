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
    def gngNodeMoved    (n: Node): Unit = ()
    def gngNodeInserted (n: Node): Unit = ()
    def gngEdgeInserted (e: Edge): Unit = ()
    def gngNodeRemoved  (n: Node): Unit = ()
    def gngEdgeRemoved  (e: Node): Unit = ()
  }
}
trait Observer {
  def gngNodeMoved    (n: Node): Unit
  def gngNodeInserted (n: Node): Unit
  def gngEdgeInserted (e: Edge): Unit
  def gngNodeRemoved  (n: Node): Unit
  def gngEdgeRemoved  (e: Node): Unit
}
