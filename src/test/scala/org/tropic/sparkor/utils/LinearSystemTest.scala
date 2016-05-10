package org.tropic.sparkor.utils

import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.rdd.RDD
import org.scalatest._
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.tropic.sparkor.TestUtils

/**
  * Created by etienne & vincent on 09/05/16.
  */
class LinearSystemTest extends FlatSpec {
  val sc = TestUtils.sc

  "A linear system with A = [1, 2, 3 ; 2, 3, 7; 7, 5, 1], b = [14, 29, 20] " should " x = [1, 2, 3] " in  {
    val features1 = Vectors.dense(Array(1.0,2.0,3.0))
    val label1 = 14.0
    val features2 = Vectors.dense(Array(2.0,3.0,7.0))
    val label2 = 29.0
    val features3 = Vectors.dense(Array(7.0,5.0,1.0))
    val label3 = 20.0
    val b: Vector = Vectors.dense(label1,label2,label3)
    val rows : RDD[Vector] = sc.parallelize(Array(features1,features2,features3))
    val mat : RowMatrix = new RowMatrix(rows)
    val xCollect = LinearSystem.solveLinearSystem(mat,b)
    println(xCollect)
    val expectedSolution = Vectors.dense(Array(1.0,2.0,3.0))
    val error = Array(xCollect.toArray(0) - expectedSolution.toArray(0),xCollect.toArray(1) - expectedSolution.toArray(1), xCollect.toArray(2) - expectedSolution.toArray(2))
    println("error: "+error.toVector)
    assert(error(0)<10e-7)
    assert(error(1)<10e-7)
    assert(error(2)<10e-7)
    

  }

  " A linear system with A = [1, 2, 3 ; 2, 3, 7; 7, 5, 1], b = [14, 29, 20] " should " Ax = [14, 29, 20]  " in {

    val features1 = Vectors.dense(Array(1.0,2.0,3.0))
    val label1 = 14.0
    val features2 = Vectors.dense(Array(2.0,3.0,7.0))
    val label2 = 29.0
    val features3 = Vectors.dense(Array(7.0,5.0,1.0))
    val label3 = 20.0
    val b: Vector = Vectors.dense(label1,label2,label3)
    val rows : RDD[Vector] = sc.parallelize(Array(features1,features2,features3))
    val mat : RowMatrix = new RowMatrix(rows)
    val xCollect = LinearSystem.solveLinearSystem(mat,b)
    val matCollect = mat.rows.collect()
    assert(matCollect(0)(0) * xCollect(0) + matCollect(0)(1) * xCollect(1) + matCollect(0)(2) * xCollect(2) - b(0) < 10e-8)
    assert(matCollect(1)(0) * xCollect(0) + matCollect(1)(1) * xCollect(1) + matCollect(1)(2) * xCollect(2) - b(1) < 10e-8)
    assert(matCollect(2)(0) * xCollect(0) + matCollect(2)(1) * xCollect(1) + matCollect(2)(2) * xCollect(2) - b(2) < 10e-8)
  }
}
