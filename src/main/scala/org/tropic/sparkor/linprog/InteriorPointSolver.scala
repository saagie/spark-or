package org.tropic.sparkor.linprog

import org.apache.spark.SparkContext
import org.tropic.sparkor.core.Solution

import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.linalg.DenseVector

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
  private var A: RowMatrix = null
  private var b: Vector = null
  private var c: Vector = null

  /**
    * Get the initialized parameters of the associated problem. They may be different from the given problem
    * @return tuple with the initialized parameters.
    */
  def getInitializedParameters : (RowMatrix, Vector, Vector) = {(A, b, c)}

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
    val n = sc.broadcast(lpb.paramA.numRows().toInt)
    val p = sc.broadcast(lpb.paramA.numCols().toInt)

    /* A = [A eye(n)] */
    A = new RowMatrix(lpb.paramA.rows.zipWithIndex().map( x => {
      var a = new Array[Double](n.value.toInt)
      a(x._2.toInt) = 1.0 // Be carefull with the sign, it depends on the constrain type
      new DenseVector(x._1.toArray ++ a)
    }))

    /* c = [c zeros(n) M] */
    c = new DenseVector(lpb.paramC.toArray ++ Array.fill[Double](n.value)(0.0) :+ 1000000000.0)

    /* b = b (No change) */
    b = lpb.paramB

    /* A = [A, b-A*ones(n,1), 1] */
    val b_broadcast = sc.broadcast(b)
    A = new RowMatrix(A.rows.zipWithIndex().map(x => new DenseVector(x._1.toArray ++ Array(b_broadcast.value.apply(x._2.toInt)-x._1.toArray.sum))))
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
