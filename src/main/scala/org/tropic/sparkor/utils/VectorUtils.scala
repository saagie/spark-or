package org.tropic.sparkor.utils

import org.apache.spark.mllib.linalg.Vector

object VectorUtils {

  /**
    * Returns true if all the values of the vector are positive
    * @param v vector of double
    * @return false if a value is negative or null
    */
  def allPositive(v: Vector): Boolean = {
    for (i <- 0 until v.size)
      if (v(i) <= 0)
        return false
    true
  }

  /**
    * Returns true if all the values of the IndexedSeq are positive
    * @param arr IndexedSeq of double
    * @return false if a value is negative or null
    */
  def allPositive(arr: IndexedSeq[Double]): Boolean = {
    for (i <- arr.indices)
      if (arr(i) <= 0)
        return false
    true
  }

  /**
    * Returns the minimum value in the vector
    * @param vectMin Vector
    * @return minimum value of this vector
    */
  def minValue(vectMin: Vector): Double = {
    var minVal = vectMin(0)
    for (i <- 1 until vectMin.size)
      if (vectMin(i) < minVal)
        minVal = vectMin(i)
    minVal
  }

  /**
    * Dot product of two vectors
    * @param firstVector First vector
    * @param secondVector Second vector
    * @return product of v and u
    */
  def dotProduct(firstVector: Vector, secondVector: Vector): Double = {
    var sum = 0.0
    //TODO: assert same size?
    //TODO: change with map function
    for (i <- 0 until firstVector.size)
      sum += firstVector(i) * secondVector(i)
    sum
  }
}
