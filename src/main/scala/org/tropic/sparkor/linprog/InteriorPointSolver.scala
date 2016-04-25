package org.tropic.sparkor.linprog

import org.apache.spark.SparkContext
import org.tropic.sparkor.core.Solution


class InteriorPointSolver(_sc: SparkContext = null) extends LinearProblemSolver(_sc) {

  var solution: Solution = null
  private var score: Double = 0

  def setInitialSolution(initSol: Option[Solution] = None): Unit = {
    solution = initSol.get
  }

  def getScore: Double = {
    score
  }

  def _cleanupSolving() {}

  def _initSolving() {}

  def _solveNIters(iterCount: Int): (Int, Solution) = {
    (0, new Solution())
  }
}
