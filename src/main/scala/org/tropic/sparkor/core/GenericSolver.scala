package org.tropic.sparkor.core

import org.apache.spark.SparkContext

/**
  * Creates the default solver for the problem
  * @param _sc SparkContext
  */
class GenericSolver(_sc: SparkContext = null) extends Solver(_sc) {

  private var pb : Problem = null
  private var solver : Solver = null

  /**
    * Sets the problem for the solver
    * @param _pb Problem to solve
    */
  def setProblem(_pb : Problem) = {
    pb = _pb
    solver = pb.generateDefaultSolver()
  }

  /**
    * Returns the score of the solution
    * @return score of the solution
    */
  def getScore: Double = {
    solver.getScore
  }

  /**
    * Cleans up the solving process
    */
  def _cleanupSolving(): Unit = {
    solver._cleanupSolving()
  }

  /**
    * Initializes the solving process
    */
  def _initSolving(): Unit = {
    solver._initSolving()
  }

  /**
    * Solves the problem within iterCount iterations
    * @param iterCount number of maximum iterations
    * @return Tuple (number of iterations to solve the problem, solution found)
    */
  def _solveNIters(iterCount: Int): (Int, Solution) = {
    solver._solveNIters(iterCount)
  }
}
