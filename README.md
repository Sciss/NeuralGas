# NeuralGas

[![Build Status](https://travis-ci.org/Sciss/NeuralGas.svg?branch=master)](https://travis-ci.org/Sciss/NeuralGas)

## statement

This project was originally forked from [github.com/gittar/demogng](https://github.com/gittar/demogng).
It was a demonstration of various topological learning algorithms,
most prominently the Growing Neural Gas (Fritzke). Original license was GNU GPL v1+.
The project has since been adapted to provide a library for
the neural gas algorithms. All changes and additions (C)opyright 2017 Hanns Holger Rutz, released under
the GNU GPL v2+.

Changes in summary:

- original authors: Hartmut S. Loos, Bernd Fritzke
- license: GNU GPL 2
- using sbt to build the application
- using dedicated namespace `de.sciss.neuralgas`
- using two sub-modules `core` (algorithms) and `ui` (AWT front-end)
- image based probability distribution
- added JFrame wrapper
- includes `sbt` script by [Paul Philips](https://github.com/paulp/sbt-extras), provided under BSD-style license
- to run demo, execute `./sbt ui/run`
