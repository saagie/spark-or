package org.tropic.sparkor


import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest._
import org.apache.spark.mllib.linalg.{Vectors, Vector, Matrices}
import org.apache.spark.{SparkContext, SparkConf}
import org.tropic.sparkor.core.{Solver, Solution}
import org.tropic.sparkor.linprog.{ConstraintType, LinearOptimizationProblem, InteriorPointSolver}

/**
  * Created by etienne on 12/05/16.
  */
class IntegrationTest extends FlatSpec {

  val conf = new SparkConf().setAppName("Tests").setMaster("local[*]")
  val sc = new SparkContext(conf)
  var xExp : Solution = new Solution()
  val xThe : Vector = Vectors.dense(1,2,8,0,0,2,0)

  def solutionFoundCallback(iter: Int, s: Solution, solver: Solver) {
    println("Solution at iteration " + iter)
    println(s.getVector)
    println(solver.getScore)
  }


  def solvingStoppedCallback(s: Solution, solver: Solver): Unit = {
    println("Final solution")
    println(s.getVector)
    println("Score")
    println(solver.getScore)
    xExp = s

  }

  " test with Interior point solver: A linear optimization problem with A = [8.0, 12.0 ; 2.0, -1.0 ; 12.0, 12.0 ; 1.0, -1.0 ], b = [24 ; 36 ; 4 ; -5] and c = [30, 20] " should " x = [1 ; 2 ; 8 ; 0 ; 0 ; 2 ; 0] " in  {
    val solver = new InteriorPointSolver(sc)
    val matA = Matrices.dense(4, 2, Array[Double](8.0, 12.0, 2.0, -1.0, 12.0, 12.0, 1.0, -1.0))
    val b = Vectors.dense(Array[Double](24, 36, 4, -5))
    val c = Vectors.dense(Array[Double](30, 20))
    var problem = new LinearOptimizationProblem(matA, b, c, ConstraintType.GreaterThan)
    solver.setProblem(problem)
    val iterInterval = 1
    solver.setNewSolutionFoundCallback(iterInterval,solutionFoundCallback)
    solver.setSolvingStoppedCallback(solvingStoppedCallback)
    solver.solve()

    for ( i <- xThe.toArray.indices) {
      assert( xThe.toArray(i) - xExp.getVector.toArray(i) < 1e-4)
    }


  }


  " test with generic solver: A linear optimization problem with A = [8.0, 12.0 ; 2.0, -1.0 ; 12.0, 12.0 ; 1.0, -1.0 ], b = [24 ; 36 ; 4 ; -5] and c = [30, 20] " should " x = [1 ; 2 ; 8 ; 0 ; 0 ; 2 ; 0] " in  {

    val matA = Matrices.dense(4, 2, Array[Double](8.0, 12.0, 2.0, -1.0, 12.0, 12.0, 1.0, -1.0))
    val b = Vectors.dense(Array[Double](24, 36, 4, -5))
    val c = Vectors.dense(Array[Double](30, 20))
    val problem = new LinearOptimizationProblem(matA, b, c, ConstraintType.GreaterThan)
    val solver = problem.generateDefaultSolver(sc)
    val iterInterval = 1
    solver.setNewSolutionFoundCallback(iterInterval,solutionFoundCallback)
    solver.setSolvingStoppedCallback(solvingStoppedCallback)
    solver.solve()

    for ( i <- xThe.toArray.indices) {
      assert( xThe.toArray(i) - xExp.getVector.toArray(i) < 1e-4)
    }


  }


}
