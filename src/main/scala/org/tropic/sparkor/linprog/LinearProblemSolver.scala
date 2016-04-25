package org.tropic.sparkor.linprog

import org.apache.spark.SparkContext
import org.tropic.sparkor.core.Solver

/**
  * Abstract class extending Solver specific to solve linear optimization problems
  * @param _sc SparkContext
  */
abstract class LinearProblemSolver(_sc: SparkContext = null) extends Solver(_sc) {

  protected var lpb: LinearOptimizationProblem = null

  /**
    * Sets the linear optimization problem to be solved
    * @param _lpb linear optimization problem to be solved
    */
  def setProblem(_lpb: LinearOptimizationProblem) {
    lpb = _lpb
  }
}
