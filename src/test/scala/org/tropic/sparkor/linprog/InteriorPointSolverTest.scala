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

import org.scalatest._
import org.tropic.sparkor.TestUtils
import org.apache.spark.mllib.linalg.{DenseMatrix, DenseVector, Vectors}
import org.tropic.sparkor.core.Solution

class InteriorPointSolverTest extends FlatSpec {

  val sc = TestUtils.sc

  "A linear problem with A = [1, 2, 3 ; 1, 2, 3], b = [2, 2], c = [5, 5, 5] " should " with ConstraintType = GreaterThan, be correctly initialized " in  {
    val paramA = new DenseMatrix(2, 3, Array(1.0, 1.0) ++ Array(2.0, 2.0) ++ Array(3.0, 3.0))
    val paramB = new DenseVector(Array(2, 2))
    val paramC = new DenseVector(Array(5, 5, 5))
    
    val solver = new InteriorPointSolver(sc)
    val pb = new LinearOptimizationProblem(paramA, paramB, paramC, ConstraintType.GreaterThan)
    solver.setProblem(pb)
    solver._initSolving()
    val params = solver._getInternParameters
    val A = params._1
    val b = params._2
    val c = params._3
    val x = params._4

    val AArray = A.toArray
    println(AArray.mkString(" "))
    assert(AArray.slice(0, 2).sameElements(Array(1, 1)))
    assert(AArray.slice(2, 4).sameElements(Array(2, 2)))
    assert(AArray.slice(4, 6).sameElements(Array(3, 3)))
    assert(AArray.slice(6, 8).sameElements(Array(-1, 0)))
    assert(AArray.slice(8, 10).sameElements(Array(0, -1)))
    assert(AArray.slice(10, 12).sameElements(Array(-3, -3)))

    assert(b.equals(new DenseVector(Array(2, 2))))
    assert(c.equals(new DenseVector(Array(5, 5, 5, 0, 0, 1.0E9))))

    /* assert A*x = b */
    val Ax = A.multiply(x)
    //assert(b.equals(Ax))
    val norm = (Ax.toArray.zip(b.toArray).map(x => x._1-x._2)).reduce((x1, x2) => x1+ x2)
    assert(norm<0.0001)

  }

  it should " with ConstraintType = Equal, be correctly initialized " in  {
    val paramA = new DenseMatrix(2, 3, Array(1.0, 1.0) ++ Array(2.0, 2.0) ++ Array(3.0, 3.0))
    val paramB = new DenseVector(Array(2, 2))
    val paramC = new DenseVector(Array(5, 5, 5))

    val solver = new InteriorPointSolver(sc)
    val pb = new LinearOptimizationProblem(paramA, paramB, paramC, ConstraintType.Equal)
    solver.setProblem(pb)
    solver._initSolving()
    val params = solver._getInternParameters
    val A = params._1
    val b = params._2
    val c = params._3
    val x = params._4

    val AArray = A.toArray
    println(AArray.mkString(" "))
    assert(AArray.slice(0, 2).sameElements(Array(1, 1)))
    assert(AArray.slice(2, 4).sameElements(Array(2, 2)))
    assert(AArray.slice(4, 6).sameElements(Array(3, 3)))
    assert(AArray.slice(6, 8).sameElements(Array(-4, -4)))

    assert(b.equals(new DenseVector(Array(2, 2))))
    assert(c.equals(new DenseVector(Array(5, 5, 5, 1.0E9))))

    /* assert A*x = b */
    val Ax = A.multiply(x)
    //assert(b.equals(Ax))
    val norm = (Ax.toArray.zip(b.toArray).map(x => x._1-x._2)).reduce((x1, x2) => x1+ x2)
    assert(norm<0.0001)
  }

  it should " with ConstraintType = GreaterThan and an initial solution, be correctly initialized " in  {
    val paramA = new DenseMatrix(2, 3, Array(1.0, 1.0) ++ Array(2.0, 2.0) ++ Array(3.0, 3.0))
    val paramB = new DenseVector(Array(2, 2))
    val paramC = new DenseVector(Array(5, 5, 5))

    val sol = new Solution(Vectors.dense(Array[Double](5, 4, 3)))
    val solver = new InteriorPointSolver(sc)
    val pb = new LinearOptimizationProblem(paramA, paramB, paramC, ConstraintType.GreaterThan)
    solver.setProblem(pb)
    solver.setInitialSolution(Some(sol))
    solver._initSolving()
    val params = solver._getInternParameters
    val A = params._1
    val b = params._2
    val c = params._3
    val x = params._4

    val AArray = A.toArray
    println(AArray.mkString(" "))
    assert(AArray.slice(0, 2).sameElements(Array(1, 1)))
    assert(AArray.slice(2, 4).sameElements(Array(2, 2)))
    assert(AArray.slice(4, 6).sameElements(Array(3, 3)))
    assert(AArray.slice(6, 8).sameElements(Array(-1, 0)))
    assert(AArray.slice(8, 10).sameElements(Array(0, -1)))

    assert(b.equals(new DenseVector(Array(2, 2))))
    assert(c.equals(new DenseVector(Array(5, 5, 5, 0, 0))))

    /* assert A*x = b */
    val Ax = A.multiply(x)
    val norm = (Ax.toArray.zip(b.toArray).map(x => x._1-x._2)).reduce((x1, x2) => x1+ x2)
    //println("**** "+norm+" *************************")
  }

  it should " with ConstraintType = Equal and an initial solution, be correctly initialized " in  {
    val paramA = new DenseMatrix(2, 3, Array(1.0, 1.0) ++ Array(2.0, 2.0) ++ Array(3.0, 3.0))
    val paramB = new DenseVector(Array(2, 2))
    val paramC = new DenseVector(Array(5, 5, 5))

    val sol = new Solution(Vectors.dense(Array[Double](5, 4, 3)))
    val solver = new InteriorPointSolver(sc)
    val pb = new LinearOptimizationProblem(paramA, paramB, paramC, ConstraintType.Equal)
    solver.setProblem(pb)
    solver.setInitialSolution(Some(sol))
    solver._initSolving()
    val params = solver._getInternParameters
    val A = params._1
    val b = params._2
    val c = params._3

    val AArray = A.toArray
    println(AArray.mkString(" "))
    assert(AArray.slice(0, 2).sameElements(Array(1, 1)))
    assert(AArray.slice(2, 4).sameElements(Array(2, 2)))
    assert(AArray.slice(4, 6).sameElements(Array(3, 3)))

    assert(b.equals(new DenseVector(Array(2, 2))))
    assert(c.equals(new DenseVector(Array(5, 5, 5))))
  }
}
