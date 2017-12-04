package de.sciss.neuralgas.ui

import javax.imageio.ImageIO

import de.sciss.file._
import de.sciss.neuralgas.ComputeGNG.Result
import de.sciss.neuralgas.{Algorithm, ComputeGNG, ImagePD}

object Cracks {
  def main(args: Array[String]): Unit = {
    val fIn                 = file("") / "data" / "projects" / "Imperfect" / "cracks" / "two_bw" / "cracks2_19bw.png"
    val compute             = new ComputeGNG
    val img                 = ImageIO.read(fIn)
    val pd                  = new ImagePD(img, true)
    compute.pd              = pd
    compute.panelWidth  = img.getWidth  / 8
    compute.panelHeight = img.getHeight / 8
    compute.maxNodes        = 10000; // pd.getNumDots / 8
    println(s"w ${compute.panelWidth}, h ${compute.panelHeight}, maxNodes ${compute.maxNodes}")
    compute.stepSize        = 400
    compute.algorithm       = Algorithm.GNGU
    compute.lambdaGNG       = 400
    compute.maxEdgeAge      = 88
    compute.epsilonGNG      = 0.1f // 0.05f
    compute.epsilonGNG2     = 6.0e-4f
    compute.alphaGNG        = 0.5f
    compute.setBetaGNG(5.0e-6f) // (1.5e-5f) // 5.0e-4f // 1.5e-5f
    compute.noNewNodesGNGB  = false
    compute.GNG_U_B         = true
    compute.utilityGNG      = 17f // 16f
    compute.autoStopB       = false
    compute.reset()
    compute.getRNG.setSeed(0L)
    compute.addNode(null)
    compute.addNode(null)

    val res             = new Result
    var lastNum         = 0
    var iter            = 0
    val t0              = System.currentTimeMillis()
    var lastT           = t0
    while (!res.stop && compute.nNodes < compute.maxNodes) {
      compute.learn(res)
      if (compute.nNodes != lastNum) {
        val t1 = System.currentTimeMillis()
        if (t1 - lastT > 4000) {
          lastNum = compute.nNodes
          lastT   = t1
          println(lastNum)
        }
      }
      iter += 1
//      if (iter == 1000) {
//        println(compute.nodes.take(compute.nNodes).mkString("\n"))
//      }
    }

    println(s"Done. Took ${(System.currentTimeMillis() - t0)/1000} seconds, and ${compute.numSignals} signals.")
    println(compute.nodes.take(compute.nNodes).mkString("\n"))
  }
}
