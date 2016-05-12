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


package org.tropic.sparkor.core

import org.apache.spark.mllib.linalg.Vector

/**
  * Represents a solution of a problem
  * @param _value value of the solution
  */
class Solution(_value: Any = null) {
  private var value = _value

  /**
    * Returns the value of the solution
    * @return value whose type is defined by the user
    */
  def getValue: Any = {
    value
  }

  /**
    * Sets the value of the solution
    * @param _value value of the solution
    */
  def setValue(_value: Any) = {
    value = _value
  }

  /**
    * Returns the value as a Spark vector.
    * @return Value of the solucation, as a Spark Vector.
    * @throws ClassCastException Thrown if the value of the solution cannot be typecast to a vector.
    */
  def getVector = {
    value.asInstanceOf[Vector]
  }
}
