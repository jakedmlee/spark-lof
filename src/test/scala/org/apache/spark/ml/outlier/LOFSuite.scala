package org.apache.spark.ml.outlier

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{DataTypes, StructField, StructType}
import org.apache.spark.sql.functions._

object LOFSuite {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("LOFExample")
      .master("local[4]")
      .getOrCreate()

    val schema = new StructType(Array(
      new StructField("col1", DataTypes.DoubleType),
      new StructField("col2", DataTypes.DoubleType)))
    val df = spark.read.schema(schema).csv("data/outlier.csv")

    val assembler = new VectorAssembler()
      .setInputCols(df.columns)
      .setOutputCol("features")
    val data = assembler.transform(df).repartition(4)

    val startTime = System.currentTimeMillis()
    val result = new LOF()
      .setMinPts(5)
      .transform(data)
    val endTime = System.currentTimeMillis()
    result.count()

    // Outliers have much higher LOF value than normal data
    result.sort(desc(LOF.lof)).head(10).foreach { row =>
      println(row.get(0) + " | " + row.get(1) + " | " + row.get(2))
    }
    println("Total time = " + (endTime - startTime) / 1000.0 + "s")
  }
}