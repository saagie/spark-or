package org.tropic.sparkor.core

abstract class Solver {
  type SolvingStartedCallback = (Solution, Solver) => Unit
  type SolvingStoppedCallback = (Solution, Solver) => Unit
  type NewSolutionFoundCallback = (Int, Solution, Solver) => Unit

  private var newSolutionFoundCallback : NewSolutionFoundCallback = null
  private var solvingStartedCallback : SolvingStartedCallback = null
  private var solvingStoppedCallback : SolvingStoppedCallback = null
  private var solution : Solution = null

  //public function
  def solve() {}
  def isSolving: Boolean = {
    false
  }
  def stopSolving() {}
  def setNewSolutionFoundCallback(N: Int, callback: NewSolutionFoundCallback) {}
  def setSolvingStartedCallback(callback: SolvingStartedCallback) {}
  def setSolvingStoppedCallback(callback: SolvingStoppedCallback) {}
  def setInitialSolution(initSol: Option[Solution] = None) {}

  //protected function
  protected def getScore: Double
  protected def initSolving()
  protected def solveNIters(iterCount: Int)
  protected def cleanupSolving()

}
