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
  * Abstract class representing a solver
  * @param _sc SparkContext
  */
abstract class Solver(_sc: SparkContext = null) {
  /**
    * Callback function called when starting the solving process
    * Solver: solver used for this problem
    */
  type SolvingStartedCallback = Solver => Unit

  /**
    * Callback function called at the end of the solving process
    * Solution: final solution of the problem
    * Solver: solver used for this problem
    */
  type SolvingStoppedCallback = (Solution, Solver) => Unit

  /**
    * Callback function called every n iterations
    * Int: number of iterations (n) after which the callback is called
    * Solution: new solution after n iterations
    * Solver: solver used for this problem
    */
  type NewSolutionFoundCallback = (Int, Solution, Solver) => Unit

  private var newSolutionFoundCallback: NewSolutionFoundCallback = null
  private var solvingStartedCallback: SolvingStartedCallback = null
  private var solvingStoppedCallback: SolvingStoppedCallback = null
  private var solving: Boolean = false
  private var iterInterval: Int = 0
  var sc: SparkContext = _sc

  /**
    * Solves the problem
    */
  def solve(): Unit = {
    _initSolving()
    if(solvingStartedCallback != null)
      solvingStartedCallback(this)
    var solution: Solution = null
    var i = 0
    solving = true
    while(solving) {
      val (nIter, newSolution) = _solveNIters(iterInterval)
      i += nIter
      solution = newSolution
      if(newSolutionFoundCallback != null)
        newSolutionFoundCallback(i, solution, this)
    }
    if(solvingStoppedCallback != null)
      solvingStoppedCallback(solution, this)
  }

  /**
    * Returns a boolean to know if the problem is being solved
    * @return True if the problem is being solved, false otherwise
    */
  def isSolving: Boolean = {
    solving
  }

  /**
    * Stops solving the problem
    */
  def stopSolving(): Unit = {
    solving = false
  }

  /**
    * Sets the newSolutionFoundCallback function
    * @param iterInterval maximum number of iterations before calling the callback
    * @param callback newSolutionFoundCallback function
    */
  def setNewSolutionFoundCallback(iterInterval: Int, callback: NewSolutionFoundCallback): Unit = {
    this.iterInterval = iterInterval
    this.newSolutionFoundCallback = callback
  }

  /**
    * Sets the solvingStartedCallback function
    * @param callback solvingStartedCallback function
    */
  def setSolvingStartedCallback(callback: SolvingStartedCallback): Unit = {
    this.solvingStartedCallback = callback
  }

  /**
    * Sets the solvingStoppedCallback function
    * @param callback solvingStoppedCallback function
    */
  def setSolvingStoppedCallback(callback: SolvingStoppedCallback): Unit = {
    this.solvingStoppedCallback = callback
  }

  /**
    * Returns the score of the solution
    * @return score of the solution
    */
  def getScore: Double

  /**
    * Initializes the solving process
    */
  def _initSolving()

  /**
    * Solves the problem within iterCount iterations
    * @param iterCount number of maximum iterations
    * @return Tuple (number of iterations to solve the problem, solution found)
    */
  def _solveNIters(iterCount: Int): (Int, Solution)

  /**
    * Cleans up the solving process
    */
  def _cleanupSolving()

}
