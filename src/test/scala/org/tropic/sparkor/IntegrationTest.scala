package org.tropic.sparkor
/**
  * Created by etienne on 12/05/16.
  */

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest._
import org.apache.spark.mllib.linalg.{Vectors, Vector, Matrices}
import org.apache.spark.{SparkContext, SparkConf}
import org.tropic.sparkor.core.{Solver, Solution}
import org.tropic.sparkor.linprog.{ConstraintType, LinearOptimizationProblem, InteriorPointSolver}


class IntegrationTest extends FlatSpec {

  val conf = new SparkConf().setAppName("Tests").setMaster("local[*]")
  val sc = new SparkContext(conf)
  var xExp : Solution = new Solution()
  val xThe : Vector = Vectors.dense(1.0,2.0)
  var score : Double = 0
  val scoreExpected = 70
  val iterInterval = 1
  val matA = Matrices.dense(4, 2, Array[Double](8.0, 12.0, 2.0, -1.0, 12.0, 12.0, 1.0, -1.0))
  val vectB = Vectors.dense(Array[Double](24, 36, 4, -5))
  val vectC = Vectors.dense(Array[Double](30, 20))


  def solutionFoundCallback(iter: Int, s: Solution, solver: Solver) {
    println("Solution at iteration " + iter)
    println(s.getVector)
    println(solver.getScore)
  }


  def solvingStoppedCallback(sol: Solution, solver: Solver): Unit = {
    println("Final solution")
    println(sol.getVector)
    println("Score")
    println(solver.getScore)
    xExp = sol
    score = solver.getScore
  }

  " test with Interior point solver: A linear optimization problem with A = [8.0, 12.0 ; 2.0, -1.0 ; 12.0, 12.0 ; 1.0, -1.0 ], b = [24 ; 36 ; 4 ; -5] and c = [30, 20] " should " x = [1 ; 2 ; 8 ; 0 ; 0 ; 2 ; 0] " in  {
    val solver = new InteriorPointSolver(sc)
    val problem = new LinearOptimizationProblem(matA, vectB, vectC, ConstraintType.GreaterThan)
    solver.setProblem(problem)
    solver.setNewSolutionFoundCallback(iterInterval,solutionFoundCallback)
    solver.setSolvingStoppedCallback(solvingStoppedCallback)
    solver.solve()
    var error = 0.0
    for ( i <- xThe.toArray.indices) {
      error += (xThe.toArray(i) - xExp.getVector.toArray(i))*(xThe.toArray(i) - xExp.getVector.toArray(i))
    }
    assert((scoreExpected - score)/scoreExpected < 1e-4)
    assert(error < 1e-6)
  }


  " test with generic solver: A linear optimization problem with A = [8.0, 12.0 ; 2.0, -1.0 ; 12.0, 12.0 ; 1.0, -1.0 ], b = [24 ; 36 ; 4 ; -5] and c = [30, 20] " should " x = [1 ; 2 ; 8 ; 0 ; 0 ; 2 ; 0] " in  {
    val problem = new LinearOptimizationProblem(matA, vectB, vectC, ConstraintType.GreaterThan)
    val solver = problem.generateDefaultSolver(sc)
    solver.setNewSolutionFoundCallback(iterInterval,solutionFoundCallback)
    solver.setSolvingStoppedCallback(solvingStoppedCallback)
    solver.solve()
    var error = 0.0
    for ( i <- xThe.toArray.indices) {
      error += (xThe.toArray(i) - xExp.getVector.toArray(i))*(xThe.toArray(i) - xExp.getVector.toArray(i))
    }
    assert((scoreExpected - score)/scoreExpected < 1e-4)
    assert(error < 1e-6)
  }


}
