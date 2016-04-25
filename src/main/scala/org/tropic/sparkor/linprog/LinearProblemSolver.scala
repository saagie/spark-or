package org.tropic.sparkor.linprog

import org.apache.spark.SparkContext
import org.tropic.sparkor.core.Solver

abstract class LinearProblemSolver(_sc: SparkContext = null) extends Solver(_sc) {
  protected var lpb: LinearOptimizationProblem = null

  def setProblem(_lpb: LinearOptimizationProblem) {
    lpb = _lpb
  }
}
