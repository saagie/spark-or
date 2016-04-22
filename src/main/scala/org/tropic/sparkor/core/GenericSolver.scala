package org.tropic.sparkor.core


class GenericSolver(_pb: Problem = null) extends Solver {
  private var pb = _pb
  private var solver : Solver = null

  def setProblem(_pb : Problem) = {
    pb = _pb
  }

  protected def cleanupSolving {}

  protected def getScore: Double = {
    0.0
  }

  protected def initSolving {}

  protected def solveNIters(iterCount: Int) {}
}
