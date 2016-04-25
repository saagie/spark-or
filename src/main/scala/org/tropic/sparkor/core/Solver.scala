package org.tropic.sparkor.core

import org.apache.spark.SparkContext

abstract class Solver(_sc: SparkContext = null) {
  type SolvingStartedCallback = Solver => Unit
  type SolvingStoppedCallback = (Solution, Solver) => Unit
  type NewSolutionFoundCallback = (Int, Solution, Solver) => Unit

  private var newSolutionFoundCallback: NewSolutionFoundCallback = null
  private var solvingStartedCallback: SolvingStartedCallback = null
  private var solvingStoppedCallback: SolvingStoppedCallback = null
  private var solving: Boolean = false
  private var iterInterval: Int = 0
  var sc: SparkContext = _sc

  def solve(): Unit = {
    _initSolving()
    solvingStartedCallback(this)
    var solution: Solution = null
    while(solving) {
      val (nIter, newSolution) = _solveNIters(iterInterval)
      solution = newSolution
      newSolutionFoundCallback(nIter, solution, this)
    }
    solvingStoppedCallback(solution, this)
  }

  def isSolving: Boolean = {
    solving
  }

  def stopSolving(): Unit = {
    solving = false
  }

  def setNewSolutionFoundCallback(iterInterval: Int, callback: NewSolutionFoundCallback): Unit = {
    this.iterInterval = iterInterval
    this.newSolutionFoundCallback = callback
  }

  def setSolvingStartedCallback(callback: SolvingStartedCallback): Unit = {
    this.solvingStartedCallback = callback
  }

  def setSolvingStoppedCallback(callback: SolvingStoppedCallback): Unit = {
    this.solvingStoppedCallback = callback
  }

  def getScore: Double
  def _initSolving()
  def _solveNIters(iterCount: Int): (Int, Solution)
  def _cleanupSolving()

}