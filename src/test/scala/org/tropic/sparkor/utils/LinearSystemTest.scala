package org.tropic.sparkor.utils


import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.rdd.RDD
import org.scalatest._
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.mllib.linalg.{Vectors, Vector}


/**
  * Created by etienne & vincent on 09/05/16.
  */
class LinearSystemTest extends FlatSpec {

  val conf = new SparkConf().setAppName("Linear system solving test").setMaster("local[*]")
  val sc = new SparkContext(conf)

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
    assert(error(0)<10e-7)
    assert(error(1)<10e-7)
    assert(error(2)<10e-7)
    println("error: "+error.toVector)
  }

}
