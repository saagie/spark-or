package org.tropic.sparkor.linprog

import org.apache.spark.mllib.linalg.{Matrix, Vector}
import org.tropic.sparkor.core.{Problem, Solver}

/**
  * Enumeration to represent the constraint type of a linear optimization problem
  */
object ConstraintType extends Enumeration {
  type ConstraintType = Value
  /**
    * Different constraint types
    */
  val Equal, GreaterThan = Value
}

import ConstraintType._

/**
  * Class representing a linear optimization problem which can be expressed in the following form:
  * min c'x
  * subject to Ax = b (or >=)
  * and x >= 0
  * @param _paramA n-by-m matrix A
  * @param _paramB vector b with n elements
  * @param _paramC vector c with m elements
  * @param _constraintType constraint type in Ax [constraintType] b. (= or <=)
  */
class LinearOptimizationProblem(_paramA: Matrix, _paramB: Vector, _paramC: Vector, _constraintType: ConstraintType) extends Problem {
  /**
    * n-by-m matrix A parameter of the problem
    */
  val paramA = _paramA
  /**
    * vector b with n elements of the problem
    */
  val paramB = _paramB
  /**
    * vector c with m elements of the problem
    */
  val paramC = _paramC
  /**
    * constraint type in Ax [constraintType] b. (=, <= or >=)
    */
  val constraintType = _constraintType

  /**
    * Generates a default solver among all the solvers existing for this problem
    * @return default solver
    */
  def generateDefaultSolver(): Solver = {
    new InteriorPointSolver()
  }
}
