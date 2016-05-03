package org.tropic.sparkor.linprog

import org.scalatest._
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.linalg.DenseVector

class InteriorPointSolverTest extends FlatSpec {

  "A linear problem with A = [1, 2, 3 ; 1, 2, 3], b = [2, 2], c = [5, 5, 5]" should " be initialize correctly" in  {
    val conf = new SparkConf().setAppName("Initialization test").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val paramA = new RowMatrix(sc.parallelize(Array.fill(2)(new DenseVector(Array(1, 2, 3)))))
    val paramB = new DenseVector(Array(2, 2))
    val paramC = new DenseVector(Array(5, 5, 5))
    
    val solver = new InteriorPointSolver(sc)
    val pb = new LinearOptimizationProblem(paramA, paramB, paramC, ConstraintType.GreaterThan)
    solver.setProblem(pb)
    solver._initSolving()
    val params = solver.getInitializedParameters
    val A = params._1
    val b = params._2
    val c = params._3

    val AArray = A.rows.collect()
    assert(AArray(0).equals(new DenseVector(Array(1, 2, 3, 1, 0, -5))))
    assert(AArray(1).equals(new DenseVector(Array(1, 2, 3, 0, 1, -5))))
    assert(b.equals(new DenseVector(Array(2, 2))))
    assert(c.equals(new DenseVector(Array(5, 5, 5, 0, 0, 1.0E9))))
  }
}