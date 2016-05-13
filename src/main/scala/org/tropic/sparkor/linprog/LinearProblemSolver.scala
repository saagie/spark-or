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


package org.tropic.sparkor.linprog

import org.apache.spark.SparkContext
import org.tropic.sparkor.core.Solver

/**
  * Abstract class extending Solver specific to solve linear optimization problems
  * @param _sc SparkContext
  */
abstract class LinearProblemSolver(_sc: SparkContext = null) extends Solver(_sc) {

  protected var lpb: LinearOptimizationProblem = null

  /**
    * Sets the linear optimization problem to be solved
    * @param _lpb linear optimization problem to be solved
    */
  def setProblem(_lpb: LinearOptimizationProblem) {
    lpb = _lpb
  }
}
