package com.renarde.wikiflow.consumer

import java.util.Properties

import com.typesafe.scalalogging._
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._

object LegacyConsumer extends App with LazyLogging {

  val appName: String = "legacy-streaming-example"
  val spark: SparkSession = SparkSession.builder()
    .appName(appName)
    .master("local[2]")
    .config("spark.driver.memory","1g")
    .getOrCreate()

  logger.info("Initializing FlowConsumer")

  spark.sparkContext.setLogLevel("WARN")

  import spark.implicits._

  val streamingContext = new StreamingContext(spark.sparkContext, Seconds(5))

  logger.info("Spark streaming context started")

  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "kafka:9092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> "consumer",
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (false: java.lang.Boolean)
  )


  val topics = Array("wikiflow-topic")

  val messageStream = KafkaUtils.createDirectStream[String, String](
    streamingContext,
    PreferConsistent,
    Subscribe[String, String](topics, kafkaParams)
  )

  val connectionProperties = new Properties()

  val jdbcUrl = s"jdbc:postgresql://postgres:5432/consumer"
  val driverClassName = "org.postgresql.Driver"

  connectionProperties.put("user", "consumer")
  connectionProperties.put("password", "goto@Postgres1")
  connectionProperties.setProperty("Driver", driverClassName)

  val typeCalculator = messageStream.transform { rdd =>
    val df = spark.read.json(rdd.map(x => x.value))

    logger.warn("==="*10)
    df.schema.foreach(field => logger.warn(s"Field data: ${field.name} with type ${field.dataType.simpleString}"))
    logger.warn("==="*10)

    if (df.columns.contains("bot")) { // check the input data for structure
      logger.warn("Data structure is correct, processing the batch")
      val typeCounter = df.filter(($"bot" === false) and ($"type" =!= "142")).groupBy($"type").count
      typeCounter.rdd.map(row => (row(0).toString, row(1).asInstanceOf[Long]))
    } else {
      logger.warn("Bad input structure in input RDD")
      spark.sparkContext.emptyRDD[(String, Long)] // missing structure error handling
    }
  }

  val typeReducer = typeCalculator.reduceByKeyAndWindow(
    (a: Long, b: Long) => a + b, // simple sum operation
    Seconds(60 * 5), // window length (last X seconds of data)
    Seconds(10) // slide interval (how often no reduce)
  )

  typeReducer.foreachRDD { rdd =>
    val typesCounterDF = rdd.toDF("type", "count").selectExpr("*", "CURRENT_TIMESTAMP() as load_dttm")
    typesCounterDF.write.mode(SaveMode.Append).jdbc(jdbcUrl, "types_count", connectionProperties)
    logger.warn("New chunk of data produced to postgres")
  }

  streamingContext.start()
  streamingContext.awaitTermination()
}
