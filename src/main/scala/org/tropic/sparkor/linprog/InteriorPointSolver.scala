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
class InteriorPointSolver(_sc: SparkContext) extends LinearProblemSolver(_sc) {
  /**
    * Solution: solution of the problem
    */
  private var hasInitSol: Boolean = false
  private var solution: Solution = new Solution()
  private var score: Double = 0
  private var matA: Broadcast[Matrix] = null
  private var vectB: Broadcast[Vector] = null
  private var vectC: Broadcast[Vector] = null
  private var vectX: Vector = null
  private val M: Double = 1e9
  private var epsilon: Double = 0
  private val Mepsilon: Double = 1e-5

  /**
    * Get the initialized parameters of the associated problem. They may be different from the given problem
    *
    * @return tuple with the initialized parameters.
    */
  def _getInternParameters : (Matrix, Vector, Vector, Vector) = {(matA.value, vectB.value, vectC.value, vectX)}

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
    if(VectorUtils.allPositive(vectX)) {
      score = VectorUtils.dotProduct(lpb.paramC, vectX)
      if (vectX(vectX.size - 1) > Mepsilon)
        score = score + vectX(vectX.size - 1) * M
    }
    else
      score = Double.PositiveInfinity
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

    var matA_tmp: Array[Double] = null
    var vectB_tmp: Array[Double] = null
    var vectC_tmp: Array[Double] = null

    if (lpb.constraintType == ConstraintType.GreaterThan) {
      /* matA = [matA eye(n)] */
      val ones = Matrices.diag(new DenseVector(Array.fill(n)(-1.0)))
      matA_tmp = lpb.paramA.toArray ++ ones.toArray
      p_tmp = p_tmp + n

      if (!hasInitSol) {
        /* c = [c zeros(n) M] */
        vectC_tmp = lpb.paramC.toArray ++ Array.fill[Double](n)(0.0) :+ M
        vectX = new DenseVector(Array.fill(n+p+1)(1))
      } else {
        /* c = [c zeros(n)] */
        vectC_tmp = lpb.paramC.toArray ++ Array.fill[Double](n)(0.0)
        val b_minus_Ax = lpb.paramB.toArray.zip(lpb.paramA.multiply(solution.getVector.toDense).toArray).map(x => x._1 - x._2)
        vectX = new DenseVector(solution.getVector.toArray ++ b_minus_Ax)
      }

    } else {
      // Constraint is Equal
      matA_tmp = lpb.paramA.toArray
      if (!hasInitSol) {
        /* c = [c M] */
        vectC_tmp = lpb.paramC.toArray :+ M
        vectX = new DenseVector(Array.fill(p+1)(1))
      } else {
        /* c = [c] */
        vectC_tmp = lpb.paramC.toArray
        vectX = solution.getVector
      }
    }

    if (!hasInitSol) {
      /* matA = [matA, b-matA*ones(n,1)] */
      val A_Matrix = new DenseMatrix(n, p_tmp, matA_tmp)
      vectB_tmp = lpb.paramB.toArray
      val b_minus_Aones = vectB_tmp.zip(A_Matrix.multiply(new DenseVector(Array.fill(p_tmp)(1))).toArray).map(x => x._1 - x._2)
      matA = sc.broadcast(Matrices.horzcat(Array(A_Matrix, Matrices.dense(n, 1, b_minus_Aones))))
      /* x = ones(p+1) */
      vectX = Vectors.dense(Array.fill(p_tmp + 1)(1.0))
    } else {
      matA = sc.broadcast(new DenseMatrix(n, p_tmp, matA_tmp))
      /* x = ones(p) */
      vectX = Vectors.dense(Array.fill(p_tmp)(1.0))
    }
    vectB = sc.broadcast(lpb.paramB)
    vectC = sc.broadcast(new DenseVector(vectC_tmp))
  }


  /**
    * Solves the problem within iterCount iterations
    *
    * @param maxIter number of maximum iterations
    * @return Tuple (number of iterations to solve the problem, solution found)
    */
  def _solveNIters(maxIter: Int): (Int, Solution) = {
    val n = matA.value.numRows
    val m = matA.value.numCols

    val epsStop = 1e-6
    val eps = 1e-6
    val stepCoef = 0.99
    var iterCount = 0

    val matAt: DenseMatrix = matA.value.transpose.asInstanceOf[DenseMatrix]

    while (isSolving && iterCount < maxIter) {
      /* X2 = x^2 */
      val vectX2 = Vectors.dense((for (i <- 0 until m) yield vectX(i) * vectX(i)).toArray)
      /* AX2 = A * x^2 */
      val matAX2 = MatrixUtils.diagMult(matA.value, vectX2)
      /* ax2at = A * Xk2 * A' */
      val matAX2At = matAX2.multiply(matAt)

      /*val breezeAx2at = new breeze.linalg.DenseMatrix(ax2at.numRows, ax2at.numCols, ax2at.toArray)
      val breezeAx2c = new breeze.linalg.DenseVector((AX2.multiply(c.value)).toArray)
      val breezeW = breezeAx2at \ breezeAx2c
      val w = new DenseVector(breezeW.toArray)*/
      val w = LinearSystem.solveLinearSystem(new RowMatrix(MatrixUtils.matrixToRDD(matAX2At, sc)), matAX2.multiply(vectC.value))
      if (w == null) {
        println("Descent direction too small, converged.")
        stopSolving()
      }
      else {
        val vectAw = matAt.multiply(w.asInstanceOf[DenseVector])
        val r = for (i <- 0 until m) yield vectC.value(i) - vectAw(i)

        val dy = Vectors.dense((for (i <- 0 until m) yield -vectX(i) * r(i)).toArray)
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
            vectX = Vectors.dense((for (i <- 0 until m) yield vectX(i) + step * vectX(i) * dy(i)).toArray)
            iterCount += 1
          }
        }
      }
    }
    solution.setValue(Vectors.dense(vectX.toArray.slice(0, lpb.paramA.numCols)))
    (iterCount, solution)
  }
}
