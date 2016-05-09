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
