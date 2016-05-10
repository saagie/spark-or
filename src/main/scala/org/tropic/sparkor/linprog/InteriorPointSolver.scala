package org.tropic.sparkor.linprog

import org.apache.spark.SparkContext
import org.tropic.sparkor.core.Solution
import org.apache.spark.mllib.linalg._
import org.apache.spark.broadcast.Broadcast

/**
  * Solver for linear optimization problems using the interior point method
  *
  * @param _sc SparkContext
  */
class InteriorPointSolver(_sc: SparkContext = null) extends LinearProblemSolver(_sc) {
  /**
    * Solution: solution of the problem
    */
  private var hasInitSol: Boolean = false
  private var solution: Solution = null
  private var score: Double = 0
  private var A: Broadcast[Matrix] = null
  private var b: Broadcast[Vector] = null
  private var c: Broadcast[Vector] = null
  private var x: Vector = null

  /**
    * Get the initialized parameters of the associated problem. They may be different from the given problem
    *
    * @return tuple with the initialized parameters.
    */
  def _getInternParameters : (Matrix, Vector, Vector, Vector) = {(A.value, b.value, c.value, x)}

  /**
    * Sets an optional initial solution of this linear optimization problem
    *
    * @param initSol Initial solution. Its value type must be a Vector[Double] which has the same size as the c vector.
    * @note The initial solution must respect the problem's constraints. Otherwise, there will be an undefined behaviour in the resolution.
    */
  def setInitialSolution(initSol: Option[Solution] = None): Unit = {
    solution = initSol match {
      case Some(sol) => {hasInitSol = true ; sol}
      case None => null
    }
  }

  /**
    * Returns the score of the solution
    *
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
    val n = lpb.paramA.numRows
    val p = lpb.paramA.numCols
    var p_tmp = p

    var A_tmp: Array[Double] = null
    var b_tmp: Array[Double] = null
    var c_tmp: Array[Double] = null

    if (lpb.constraintType == ConstraintType.GreaterThan) {
      /* A = [A eye(n)] */
      val ones = Matrices.diag(new DenseVector(Array.fill(n)(-1.0)))
      A_tmp = lpb.paramA.toArray ++ ones.toArray
      p_tmp = p_tmp + n

      if (!hasInitSol) {
        /* c = [c zeros(n) M] */
        c_tmp = lpb.paramC.toArray ++ Array.fill[Double](n)(0.0) :+ 1000000000.0
        x = new DenseVector(Array.fill(n+p+1)(1))
      } else {
        /* c = [c zeros(n)] */
        c_tmp = lpb.paramC.toArray ++ Array.fill[Double](n)(0.0)
        val Ax_minus_b = lpb.paramB.toArray.zip(lpb.paramA.multiply(solution.getVector().toDense.asInstanceOf[DenseVector]).toArray).map(x => x._2 - x._1)
        x = new DenseVector(solution.getVector().toArray ++ Ax_minus_b)
      }

    } else {
      // Constraint is Equal
      A_tmp = lpb.paramA.toArray
      if (!hasInitSol) {
        /* c = [c M] */
        c_tmp = lpb.paramC.toArray :+ 1000000000.0
        x = new DenseVector(Array.fill(p+1)(1))
      } else {
        /* c = [c] */
        c_tmp = lpb.paramC.toArray
        x = solution.getVector()
      }
    }

    if (!hasInitSol) {
      /* A = [A, b-A*ones(n,1)] */
      val A_Matrix = new DenseMatrix(n, p_tmp, A_tmp)
      b_tmp = lpb.paramB.toArray
      val b_minus_Aones = b_tmp.zip(A_Matrix.multiply(new DenseVector(Array.fill(p_tmp)(1))).toArray).map(x => x._1 - x._2)
      A = sc.broadcast(Matrices.horzcat(Array(A_Matrix, Matrices.dense(n, 1, b_minus_Aones))))
    } else {
      A = sc.broadcast(new DenseMatrix(n, p_tmp, A_tmp))
    }

    b = sc.broadcast(lpb.paramB)
    c = sc.broadcast(new DenseVector(c_tmp))
    
}



/**
  * Solves the problem within iterCount iterations
  *
  * @param iterCount number of maximum iterations
  * @return Tuple (number of iterations to solve the problem, solution found)
  */
def _solveNIters(iterCount: Int): (Int, Solution) = {
  (0, new Solution())
}
}
