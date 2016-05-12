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
