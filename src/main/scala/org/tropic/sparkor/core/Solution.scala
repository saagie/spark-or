package org.tropic.sparkor.core


class Solution(_value: Any = null) {
  private var value = _value

  def intVectorValue: Vector[Int] = {null}
  def floatVectorValue: Vector[Float] = {null}
  def getValue: Any = {
    value
  }
  def setValue(_value: Any) = {
    value = _value
  }
}
