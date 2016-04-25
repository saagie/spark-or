package org.tropic.sparkor.linprog

import org.apache.spark.rdd.RDD
import org.tropic.sparkor.core.{Problem, Solver}

object ConstraintType extends Enumeration {
  type ConstraintType = Value
  val Equal, GreaterThan, LessThan = Value
}
import ConstraintType._

class LinearOptimizationProblem(_paramA: RDD[Vector[Double]], _paramB: RDD[Double], _paramC: RDD[Double], _constaintType: ConstraintType) extends Problem {
  val paramA = _paramA
  val paramB = _paramB
  val paramC = _paramC
  val constaintType = _constaintType

  def generateDefaultSolver(): Solver = {
    new InteriorPointSolver()
  }
}
