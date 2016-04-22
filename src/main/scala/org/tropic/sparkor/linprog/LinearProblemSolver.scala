package org.tropic.sparkor.linprog

import org.tropic.sparkor.core.Solver

abstract class LinearProblemSolver extends Solver {
  private var lpb: LinearOptimizationProblem = null

  def setProblem(_lpb: LinearOptimizationProblem) {
    lpb = _lpb
  }
}
