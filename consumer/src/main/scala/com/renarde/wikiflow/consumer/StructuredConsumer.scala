package com.renarde.wikiflow.consumer

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions.{from_json, lit, current_timestamp}

object StructuredConsumer extends App with LazyLogging {
  val appName: String = "structured-consumer-example"

  val spark: SparkSession = SparkSession.builder()
    .appName(appName)
    .config("spark.driver.memory", "5g")
    .config("spark.jars.packages","org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0")
    .master("local[2]")
    .getOrCreate()

  import spark.implicits._
  logger.info("Initializing Structured consumer")

  spark.sparkContext.setLogLevel("WARN")

  val inputStream = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "kafka:9092")
    .option("subscribe", "consumer")
    .load()

  val rawData = inputStream.selectExpr("CAST(value) as STRING")


  val expectedSchema = new StructType()
    .add(StructField("bot", BooleanType))
    .add(StructField("comment", StringType))
    .add(StructField("id", LongType))
    .add("length", new StructType()
      .add(StructField("new", LongType))
      .add(StructField("old", LongType))
    )
    .add("meta", new StructType()
      .add(StructField("domain", StringType))
      .add(StructField("dt", StringType))
      .add(StructField("id", StringType))
      .add(StructField("offset", LongType))
      .add(StructField("partition", LongType))
      .add(StructField("request_id", StringType))
      .add(StructField("stream", StringType))
      .add(StructField("topic", StringType))
      .add(StructField("uri", StringType))
      .add(StructField("wiki", StringType))
    )

  val parsedData = rawData.select(from_json($"value",expectedSchema)).withColumn("occured_at",lit(current_timestamp()))

  val consoleOutput = parsedData.writeStream
    .outputMode("append")
    .format("console")
    .start()

  consoleOutput.awaitTermination()

}

