package org.tropic.sparkor.core


trait Problem {
  def generateDefaultSolver(): Solver
}