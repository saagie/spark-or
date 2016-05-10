package org.tropic.sparkor

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by nathan on 10/05/16.
  */
object TestUtils {
  val conf = new SparkConf().setAppName("Tests").setMaster("local[*]")
  val sc = new SparkContext(conf)
}
