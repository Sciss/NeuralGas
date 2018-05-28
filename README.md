# NeuralGas

[![Build Status](https://travis-ci.org/Sciss/NeuralGas.svg?branch=master)](https://travis-ci.org/Sciss/NeuralGas)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.sciss/neuralgas/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.sciss/neuralgas)

## statement

This project was originally forked from [github.com/gittar/demogng](https://github.com/gittar/demogng).
It was a demonstration of various topological learning algorithms,
most prominently the Growing Neural Gas (Fritzke). Original license was GNU GPL v1+.
The project has since been adapted to provide a library for
the neural gas algorithms. All changes and additions (C)opyright 2017 Hanns Holger Rutz.
The adapted Java project (modules `core` and `ui`) are released under the GNU GPL v2+.
A new experimental Scala module `sphere` has been added that is a new implementation of GNG-U,
using spherical coordinates. This module is released under the GNU LGPL v2.1+.

Changes to the Java project in summary:

- original authors: Hartmut S. Loos, Bernd Fritzke
- license: GNU GPL 2
- using sbt to build the application
- using dedicated namespace `de.sciss.neuralgas`
- using two sub-modules `core` (algorithms) and `ui` (AWT front-end)
- code clean up
- image based probability distribution
- added JFrame wrapper
- includes `sbt` script by [Paul Phillips](https://github.com/paulp/sbt-extras), provided under BSD-style license
- to run demo, execute `./sbt ui/run`

## building

The project builds with [sbt](http://www.scala-sbt.org/). To run the original demo, use

    sbt neuralgas-ui/run

## linking

The following artifact is available from Maven Central:

    "de.sciss" %% "neuralgas" % v
    
Modules:

    "de.sciss" %  "neuralgas-core"   % v
    "de.sciss" %  "neuralgas-ui"     % v
    "de.sciss" %% "neuralgas-sphere" % v

The current version `v` is `"2.3.2"`.
