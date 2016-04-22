package org.tropic.sparkor.core

import java.util.Optional


abstract class Solver {
  type StopCriterion = (Solver) => Boolean
  type SolvingStartedCallback = (Solution, Solver) => Unit
  type SolvingStoppedCallback = (Solution, Solver) => Unit
  type NewSolutionFoundCallback = (Int, Solution, Solver) => Unit

  private var stopCriterion : StopCriterion = null
  private var newSolutionFoundCallback : NewSolutionFoundCallback = null
  private var solvingStartedCallback : SolvingStartedCallback = null
  private var solvingStoppedCallback : SolvingStoppedCallback = null
  private var solution : Solution = null

  //function public
  def solve() {}
  def isSolving: Boolean = {
    return false
  }
  def stopSolving() {}
  def setStopCriterion(criterion: StopCriterion) {}
  def setNewSolutionFoundCallback(N: Int, callback: NewSolutionFoundCallback) {}
  def setSolvingStartedCallback(callback: SolvingStartedCallback) {}
  def setSolvingStoppedCallback(callback: SolvingStoppedCallback) {}
  def setInitialSolution(initSol: Optional[Solution] = None) {}

  //function friendly
  protected def getScore: Double
  protected def initSolving()
  protected def solveNIters(iterCount: Int)
  protected def cleanupSolving()

}
