package org.tropic.sparkor.linprog

import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.tropic.sparkor.core.Solution
import org.apache.spark.mllib.linalg._
import org.apache.spark.broadcast.Broadcast
import org.tropic.sparkor.utils._

import scala.collection.mutable.ArrayBuffer

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
  private var solution: Solution = new Solution()
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
        c_tmp = lpb.paramC.toArray ++ Array.fill[Double](n)(0.0) :+ 1000000.0
        x = new DenseVector(Array.fill(n+p+1)(1))
      } else {
        /* c = [c zeros(n)] */
        c_tmp = lpb.paramC.toArray ++ Array.fill[Double](n)(0.0)
        val b_minus_Ax = lpb.paramB.toArray.zip(lpb.paramA.multiply(solution.getVector.toDense.asInstanceOf[DenseVector]).toArray).map(x => x._1 - x._2)
        x = new DenseVector(solution.getVector.toArray ++ b_minus_Ax)
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
        x = solution.getVector
      }
    }

    if (!hasInitSol) {
      /* A = [A, b-A*ones(n,1)] */
      val A_Matrix = new DenseMatrix(n, p_tmp, A_tmp)
      b_tmp = lpb.paramB.toArray
      val b_minus_Aones = b_tmp.zip(A_Matrix.multiply(new DenseVector(Array.fill(p_tmp)(1))).toArray).map(x => x._1 - x._2)
      A = sc.broadcast(Matrices.horzcat(Array(A_Matrix, Matrices.dense(n, 1, b_minus_Aones))))
      /* x = ones(p+1) */
      x = Vectors.dense(Array.fill(p_tmp + 1)(1.0))
    } else {
      A = sc.broadcast(new DenseMatrix(n, p_tmp, A_tmp))
      /* x = ones(p) */
      x = Vectors.dense(Array.fill(p_tmp)(1.0))
    }
    b = sc.broadcast(lpb.paramB)
    c = sc.broadcast(new DenseVector(c_tmp))
  }


  /**
    * Solves the problem within iterCount iterations
    *
    * @param maxIter number of maximum iterations
    * @return Tuple (number of iterations to solve the problem, solution found)
    */
  def _solveNIters(maxIter: Int): (Int, Solution) = {
    val n = A.value.numRows
    val m = A.value.numCols

    println(A.value)
    val epsStop = 0.000001
    val eps = 0.000001
    val stepCoef = 0.99
    var iterCount = 0

    while (isSolving && iterCount < maxIter) {
      /* X2 = x^2 */
      val X2 = Vectors.dense((for (i <- 0 until m) yield x(i) * x(i)).toArray)
      /* AX2 = A * x^2 */
      val AX2 = MatrixUtils.diagMult(A.value, X2)
      val At: DenseMatrix = A.value.transpose.asInstanceOf[DenseMatrix]
      /* mult = A * Xk2 * A' */
      val ax2at = AX2.multiply(At)

      // print
      println("\nX2 \n" + X2)
      println("\nAX2 \n" + AX2)
      println("\nmult \n" + mult)


      val breezeAx2at = new breeze.linalg.DenseMatrix(ax2at.numRows, ax2at.numCols, ax2at.toArray)
      val breezeAx2c = new breeze.linalg.DenseVector((AX2.multiply(c.value)).toArray)
      val breezeW = breezeAx2at \ breezeAx2c
      val w = new DenseVector(breezeW.toArray)
      //val w = LinearSystem.solveLinearSystem(new RowMatrix(MatrixUtils.matrixToRDD(mult, sc)), AX2.multiply(c.value))
      if (w == null) {
        println("Descent direction too small, converged.")
        stopSolving()
      }
      else {
        val Aw = At.multiply(w.asInstanceOf[DenseVector])
        val r = for (i <- 0 until m) yield c.value(i) - Aw(i)

        val dy = Vectors.dense((for (i <- 0 until m) yield -x(i) * r(i)).toArray)
        val norm = Vectors.norm(dy, 2.0)

        if (VectorUtils.allPositive(r) && norm <= epsStop) {
          println("STOP: Stop criterion OK")
          stopSolving()
        }
        else {
          if (VectorUtils.allPositive(dy)) {
            println("ERROR: Unbounded")
            stopSolving()
          }
          else if (norm <= eps) {
            println("STOP: Too little step")
            stopSolving()
          }
          else {
            var minDy = VectorUtils.minValue(dy)
            val step = -stepCoef / minDy
            x = Vectors.dense((for (i <- 0 until m) yield x(i) + step * x(i) * dy(i)).toArray)
            iterCount += 1
          }
        }
      }
    }
    solution.setValue(x)
    (iterCount, solution)
  }
}
