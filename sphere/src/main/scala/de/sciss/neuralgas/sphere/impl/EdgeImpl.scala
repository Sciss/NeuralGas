/*
 *  EdgeImpl.scala
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

final class EdgeImpl(val from: NodeImpl, val to: NodeImpl) extends Edge {
  var age: Int = 0
}
