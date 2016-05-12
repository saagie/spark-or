package org.tropic.sparkor.core

import org.apache.spark.SparkContext

/**
  * Interface to represent a problem
  */
trait Problem {
  /**
    * Generates a default solver among all the solvers existing for this problem
 *
    * @return default solver
    */
  def generateDefaultSolver(sc: SparkContext): Solver
}