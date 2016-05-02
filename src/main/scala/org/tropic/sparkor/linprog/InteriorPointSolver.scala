package org.tropic.sparkor.linprog

import org.apache.spark.SparkContext
import org.tropic.sparkor.core.Solution

/**
  * Solver for linear optimization problems using the interior point method
  * @param _sc SparkContext
  */
class InteriorPointSolver(_sc: SparkContext = null) extends LinearProblemSolver(_sc) {
  /**
    * Solution: solution of the problem
    */
  private var solution: Solution = null
  private var score: Double = 0

  /**
    * Sets an optional initial solution of this linear optimization problem
    * @param initSol Initial solution. Its value type must be a Vector[Double] which has the same size as the c vector.
    */
  def setInitialSolution(initSol: Option[Solution] = None): Unit = {
    solution = initSol match {
      case Some(sol) => sol
      case None => null
    }
  }

  /**
    * Returns the score of the solution
    * @return score of the solution
    */
  def getScore: Double = {
    score
  }

  /**
    * Cleans up the solving process
    */
  def _cleanupSolving() {}

  /**
    * Initializes the solving process
    */
  def _initSolving(): Unit = {

  }

  /**
    * Solves the problem within iterCount iterations
    * @param iterCount number of maximum iterations
    * @return Tuple (number of iterations to solve the problem, solution found)
    */
  def _solveNIters(iterCount: Int): (Int, Solution) = {
    (0, new Solution())
  }
}
