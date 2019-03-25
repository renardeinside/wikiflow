package producer

import java.util.Properties

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.RestartSource
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.concurrent.duration._
import akka.http.scaladsl.model._
import com.typesafe.scalalogging._
import org.slf4j.LoggerFactory

object FlowProducer extends App {
    val logger = Logger(LoggerFactory.getLogger(this.getClass))
    logger.info("Initializing FlowProducer")
    Thread.sleep(30000)

    implicit val system: ActorSystem = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()

    import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._
    import system.dispatcher

    val props = new Properties()

    props.put("bootstrap.servers", "kafka:9092")
    props.put("client.id", "producer")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("acks", "all")
    props.put("metadata.max.age.ms","10000")

    val producer = new KafkaProducer[String, String](props)
    producer.flush

    logger.info("Kafka producer initialized")

    var msgCounter = 0

    val restartSource = RestartSource.withBackoff(
        minBackoff = 3.seconds,
        maxBackoff = 10.seconds,
        randomFactor = 0.2
    ) { () =>
        Source.fromFutureSource {
            Http().singleRequest(Get("https://stream.wikimedia.org/v2/stream/recentchange"))
            .flatMap(Unmarshal(_).to[Source[ServerSentEvent, NotUsed]])
        }
    }

    restartSource.runForeach(elem => {
        msgCounter +=1 
        val data = new ProducerRecord[String, String]("wikiflow-topic", elem.data)
        val senderFeedback = producer.send(data).get
        if (msgCounter % 100 == 0) {
            logger.info(s"New messages came, total: ${msgCounter} messages")
        }
    })
}
