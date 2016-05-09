package org.tropic.sparkor.utils

import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.linalg.{DenseVector, Matrices, Vector, Vectors}

object LinearSystem {

  def multiplyVector(colU :Vector, b : Vector ): Double ={
    var res = 0.0
    val size = colU.size
    val colUArray = colU.toArray
    val bArray = b.toArray
    for (x<-0 until size){
      res  =res+ colUArray(x) * bArray(x)
    }
    res
  }

  def multiplyScalar(ub :Array[Double], s : Vector) : Vector = {
    val size = s.size
    val sArray = s.toArray
    val resu = for (x<-0 until size) yield 1/sArray(x) * ub(x)
    Vectors.dense(resu.toArray)
  }

  def printTime[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    println("Elapsed time: " + ((t1 - t0).toDouble / 1E9).toString + "s")
    result
  }

  def rowToTransposedTriplet(row: Vector, rowIndex: Long): Array[(Long, (Long, Double))] = {
    val indexedRow = row.toArray.zipWithIndex
    indexedRow.map{case (value, colIndex) => (colIndex.toLong, (rowIndex, value))}
  }

  def buildRow(rowWithIndexes: Iterable[(Long, Double)]): Vector = {
    val resArr = new Array[Double](rowWithIndexes.size)
    rowWithIndexes.foreach{case (index, value) =>
        resArr(index.toInt) = value
    }
    Vectors.dense(resArr)
  }

  //TODO: delete this function if possible for more efficiency
  def transposeRowMatrix(m: RowMatrix): RowMatrix = {
    val transposedRowsRDD = m.rows.zipWithIndex.map{case (row, rowIndex) => rowToTransposedTriplet(row, rowIndex)}
      .flatMap(x => x) // now we have triplets (newRowIndex, (newColIndex, value))
      .groupByKey
      .sortByKey().map(_._2) // sort rows and remove row indexes
      .map(buildRow) // restore order of elements in each row and remove column indexes
    new RowMatrix(transposedRowsRDD)
  }

  def solveLinearSystem(A: RowMatrix, b: Vector): Vector = {
    val svd = A.computeSVD(A.numCols.toInt, computeU = true)
    val matU = svd.U
    val s = svd.s // The singular values are stored in a local dense vector.
    val matV = svd.V // The V factor is a local dense matrix.
    val matUtranspose = transposeRowMatrix(matU)
    val bMat = Matrices.dense(b.size, 1, b.toArray)
    val ub = matUtranspose.multiply(bMat).rows.collect
    val z = Vectors.dense((for(i <- ub.indices) yield ub(i)(0) / s(i)).toArray)
    matV.multiply(z.asInstanceOf[DenseVector])
  }
}
