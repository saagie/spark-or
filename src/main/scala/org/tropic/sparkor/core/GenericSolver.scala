/*
 *   Spark-OR version 0.0.1
 *
 *   Copyright 2016 Saagie
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */


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
    solver = pb.generateDefaultSolver(sc)
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
