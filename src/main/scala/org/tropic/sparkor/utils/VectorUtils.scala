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
    * @param v Vector
    * @return minimum value of this vector
    */
  def minValue(v: Vector): Double = {
    var minVal = v(0)
    for (i <- 1 until v.size)
      if (v(i) < minVal)
        minVal = v(i)
    minVal
  }
}
