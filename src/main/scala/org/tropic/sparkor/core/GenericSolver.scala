package org.tropic.sparkor.core

import org.apache.spark.SparkContext

class GenericSolver(_sc: SparkContext = null) extends Solver(_sc) {

  private var pb : Problem = null
  private var solver : Solver = null

  def setProblem(_pb : Problem) = {
    pb = _pb
    solver = pb.generateDefaultSolver()
  }

  def getScore: Double = {
    solver.getScore
  }

  def _cleanupSolving(): Unit = {
    solver._cleanupSolving()
  }

  def _initSolving(): Unit = {
    solver._initSolving()
  }

  def _solveNIters(iterCount: Int): (Int, Solution) = {
    solver._solveNIters(iterCount)
  }
}
