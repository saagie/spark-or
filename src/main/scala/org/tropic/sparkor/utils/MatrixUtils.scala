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


package org.tropic.sparkor.utils

import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.{DenseVector, Matrices, Matrix, Vector}
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ArrayBuffer

object MatrixUtils {

  /**
    * Converts an Array[Vector] to a Matrix
    *
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
    *
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

  /**
    * Multiplies a matrix A with a vector which represents a diagonal matrix
    * @param matA matrix
    * @param vectD vector with diagonal values of a matrix
    * @return Matrix containing the result of the multiplication
    */
  def diagMult(matA: Matrix, vectD: Vector): Matrix = {
    val n = matA.numRows
    val m = matA.numCols
    val resBuf = ArrayBuffer.fill[Double](n * m)(0.0)
    for (row <- 0 until n)
      for (col <- 0 until m)
        resBuf(row + col * n) = vectD(col) * matA(row, col)
    // res = A * d
    Matrices.dense(n, m, resBuf.toArray)
  }
}
