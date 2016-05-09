package org.tropic.sparkor.utils

import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.linalg.{Matrices, Vector, Vectors}

object LinearSystem {

  private def rowToTransposedTriplet(row: Vector, rowIndex: Long): Array[(Long, (Long, Double))] = {
    val indexedRow = row.toArray.zipWithIndex
    indexedRow.map{case (value, colIndex) => (colIndex.toLong, (rowIndex, value))}
  }
  
  private def buildRow(rowWithIndexes: Iterable[(Long, Double)]): Vector = {
    val resArr = new Array[Double](rowWithIndexes.size)
    rowWithIndexes.foreach{case (index, value) =>
      resArr(index.toInt) = value
    }
    Vectors.dense(resArr)
  }

  //TODO: delete this function if possible for more efficiency
  private def transposeRowMatrix(m: RowMatrix): RowMatrix = {
    val transposedRowsRDD = m.rows.zipWithIndex.map{case (row, rowIndex) => rowToTransposedTriplet(row, rowIndex)}
      .flatMap(x => x) // now we have triplets (newRowIndex, (newColIndex, value))
      .groupByKey
      .sortByKey().map(_._2) // sort rows and remove row indexes
      .map(buildRow) // restore order of elements in each row and remove column indexes
    new RowMatrix(transposedRowsRDD)
  }

  /**
    * solve the problem Ax=b where A as a matrix and b as a vector are the parameters and x as a vector is the solution.
    * @param A features matrix A of the problem
    * @param b Labels Vector b of the problem
    * @return x the solution of the problem
    */
  def solveLinearSystem(A : RowMatrix, b: Vector): Vector = {
    val svd = A.computeSVD(A.numCols.toInt, computeU = true)
    val matU = svd.U
    val s = svd.s // The singular values are stored in a local dense vector.
    val matV = svd.V // The V factor is a local dense matrix.
    val matUtranspose = transposeRowMatrix(matU)
    val bMat = Matrices.dense(b.size, 1, b.toArray)
    val ub = matUtranspose.multiply(bMat).rows.collect
    val z = Vectors.dense((for(i <- ub.indices) yield ub(i)(0) / s(i)).toArray)
    matV.multiply(z)
  }
}
