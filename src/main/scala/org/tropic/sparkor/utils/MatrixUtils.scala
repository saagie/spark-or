package org.tropic.sparkor.utils

import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.{DenseVector, Matrices, Matrix, Vector}
import org.apache.spark.rdd.RDD

object MatrixUtils {

  /**
    * Converts an Array[Vector] to a Matrix
    * @param arr array of vectors to convert
    * @return matrix containing arr values
    */
  def arrayToMatrix(arr: Array[Vector]): Matrix = {
    var v = new scala.collection.mutable.ArrayBuffer[Double](arr.length * arr(0).size)
    for(j <- 0 until arr(0).size)
      for(i <- arr.indices)
        v += arr(i).apply(j)
    Matrices.dense(arr.length, arr(0).size, v.toArray)
  }

  /**
    * Converts a matrix to an RDD of vectors. Useful to create new RowMatrix
    * @param m matrix to convert
    * @param sc Spark Context
    * @return RDD of vectors
    */
  def matrixToRDD(m: Matrix, sc: SparkContext): RDD[Vector] = {
    val columns = m.toArray.grouped(m.numRows)
    val rows = columns.toSeq.transpose // Skip this if you want a column-major RDD.
    val vectors = rows.map(row => new DenseVector(row.toArray))
    sc.parallelize(vectors)
  }
}