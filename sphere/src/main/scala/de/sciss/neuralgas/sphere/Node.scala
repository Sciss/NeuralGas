/*
 *  Node.scala
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

trait Node extends Loc {
  def id: Int

  def utility: Double
  def error  : Double

  def numNeighbors: Int

  def neighbor(idx: Int): Node
}
