import org.apache.spark.sql.SaveMode

// Create the JDBC URL without passing in the user and password parameters.
val jdbcUrl = s"jdbc:postgresql://localhost:5432/postgres"
val driverClassName = "org.postgresql.Driver"

Class.forName(driverClassName)

// Create a Properties() object to hold the parameters.
import java.util.Properties
val connectionProperties = new Properties()

connectionProperties.put("user", "postgres")
connectionProperties.put("password", "goto@Postgres1")
connectionProperties.setProperty("Driver", driverClassName)

val df = spark.sparkContext.parallelize(List(
    ("new",200),
    ("old",100)
)).toDF("type","count")

df.write.mode(SaveMode.Append).jdbc(jdbcUrl, "types_count", connectionProperties)



val typesCountDF = spark.read.jdbc(jdbcUrl, "types_count", connectionProperties)

//  docker exec -it wikiflow-local_consumer-dev_1 /usr/local/spark/bin/spark-shell

val inputStream = spark.readStream.format("kafka").option("kafka.bootstrap.servers", "kafka:9092").option("subscribe", "wikiflow-topic").load()
val inputStreamDF = inputStream.selectExpr("CAST(value AS STRING) as STRING").as[String].toDF()

val writer = inputStreamDF.writeStream.format("text").option("checkpointLocation", "/checkpoints").option("path", "/batches")

val writerQuery = writer.start()


writerQuery.stop()

spark.read.json("/batches").printSchema

import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

val inputSchema = new StructType()
    .add(StructField("bot",BooleanType,true)) 
    .add(StructField("comment",StringType,true))
    .add(StructField("id",LongType,true))
    .add(StructField("length", 
        new StructType()
            .add(StructField("new",LongType,true))
            .add(StructField("old",LongType,true))
        ,true))
    .add(StructField("log_action",StringType,true)) 
    .add(StructField("log_action_comment",StringType,true))
    .add(StructField("log_id",LongType,true))
    .add(StructField("log_params",StringType,true))
    .add(StructField("log_type",StringType,true))
    .add(StructField("meta", 
        new StructType()
            .add(StructField("domain",StringType,true))
            .add(StructField("dt",StringType,true))
            .add(StructField("id",StringType,true)) 
            .add(StructField("offset",LongType,true))
            .add(StructField("partition",LongType,true))
            .add(StructField("request_id",StringType,true))
            .add(StructField("schema_uri",StringType,true))
            .add(StructField("topic",StringType,true))
            .add(StructField("uri",StringType,true)),true)) 
    .add(StructField("minor",BooleanType,true))
    .add(StructField("namespace",LongType,true))
    .add(StructField("parsedcomment",StringType,true))
    .add(StructField("patrolled",BooleanType,true))
    .add(StructField("revision", 
        new StructType()
            .add(StructField("new",LongType,true))
            .add(StructField("old",LongType,true)),true)) 
    .add(StructField("server_name",StringType,true))
    .add(StructField("server_script_path",StringType,true))
    .add(StructField("server_url",StringType,true))
    .add(StructField("timestamp",LongType,true))
    .add(StructField("title",StringType,true))
    .add(StructField("type",StringType,true))
    .add(StructField("user",StringType,true)) 
    .add(StructField("wiki",StringType,true))


val inputStream = spark.readStream.format("kafka").option("kafka.bootstrap.servers", "kafka:9092").option("subscribe", "wikiflow-topic").load()

val parsedDF = inputStream.selectExpr("CAST(value AS STRING) as json").select(from_json($"json", schema=inputSchema).as("data")).select("data.*")
val typesGrouped = parsedDF.filter(($"bot" === false) and ($"type" =!= "142")).groupBy($"type").count

val writerQuery = typesGrouped 
    .selectExpr("*","CURRENT_TIMESTAMP() as timestamp")
    // .withWatermark("timestamp","1 minutes")
    .writeStream.outputMode("complete").format("console").start()

writerQuery.stop()

