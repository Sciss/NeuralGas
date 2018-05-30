/*
 *  SphereGNG.scala
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


import de.sciss.neuralgas.sphere.SphereGNG.Config

object SphereGNG {
  /** Configuration of the algorithm.
    *
    * @param pd         probability distribution function
    * @param epsilon    adaptation factor for 'winner'
    *                   (factor of moving winner node towards pd-emitted position)
    * @param epsilon2   adaptation factor for neighbours of 'winner'
    *                   (factor of moving neighbour nodes towards pd-emitted position)
    * @param beta       controls decay of errors and utilities (decay is `1.0 - beta`)
    * @param alpha      controls decay of errors and utilities for winning nodes
    *                   (when inserting a new node between them; decay is `1.0 - alpha`)
    * @param lambda     probability of inserting a node (each step)
    * @param utility    scaling factor such that a node is deleted when
    *                   `utility * node-utility < max-error`
    * @param seed       random number generator initial seed value
    * @param maxNodes0  initial maximum number of nodes. note that when changing `maxNodes`
    *                   later, the values are always clipped to `maxNodes0` (there will never
    *                   be more than `maxNodes0` nodes).
    */
  final case class Config(
                           pd           : PD        = PD.Uniform,
                           epsilon      : Double    = 0.02,
                           epsilon2     : Double    = 0.01,
                           beta         : Double    = 0.001,
                           alpha        : Double    = 0.001,
                           lambda       : Double    = 1.0/100,
                           utility      : Double    = 4.0,
                           seed         : Long      = 0L,
                           maxNodes0    : Int       = 1000,
                           maxEdges0    : Int       = 6000,
                           maxEdgeAge   : Int       = 80,
                           maxNeighbors : Int       = 10,
                           observer     : Observer  = Observer.Dummy
                         )

  def apply(config: Config): SphereGNG = impl.SphereGNGImpl(config)
}

/** A variant of the Growing Neural Gas with Utility (GNG-U) algorithm that uses
  * spherical coordinates. Other changes include:
  *
  * - instead of an iteration count for 'lambda' for new node insertion, we use a probability
  */
trait SphereGNG {
  def config: Config

  def step(): Unit

  var maxNodes: Int

  def nodeIterator: Iterator[Node]
  def edgeIterator: Iterator[Edge]
}