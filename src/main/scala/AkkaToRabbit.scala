import java.nio.file.Paths

import akka.{Done}
import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.alpakka.amqp.{AmqpWriteSettings, QueueDeclaration}
import akka.stream.alpakka.amqp.scaladsl.AmqpSink
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.util.ByteString
import ProductRequirements.{fitWithProductRequirements, getTargetedValue}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object AkkaToRabbit extends App {
  implicit val system = ActorSystem("AkkaToRabbit")
  // implicit val actorSystem: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty, "alpakka-samples")
  implicit val dispatcher = system.dispatchers.lookup("akka.actor.dedicated-dispatcher")

  val sourceIO = FileIO.fromPath(Paths.get("src/main/resources/data/title.basics.tsv")).async

  def filterSource(source: Source[ByteString, Future[IOResult]]) = source
    .via(CsvParsing.lineScanner(delimiter='\t')).async
    .via(CsvToMap.toMapAsStrings()).async
    .filter(fitWithProductRequirements).async
    .map(getTargetedValue).async

  val connectionProvider = RabbitMQConnect.getConnection
  val queueName = RabbitMQConnect.RABBITMQ_QUEUE
  val queueDeclaration = QueueDeclaration(queueName)

  val amqpSink: Sink[ByteString, Future[Done]] =
    AmqpSink.simple(
      AmqpWriteSettings(connectionProvider)
        .withRoutingKey(queueName)
        .withDeclaration(queueDeclaration)
    )

  val sinkWriter: Future[Done] = filterSource(sourceIO).runWith(amqpSink)
  sinkWriter.onComplete {
    case Success(done) =>
      println(s"$done")
      system.terminate()
    case Failure(e) => println(s"An issue has occurred: $e")
  }
}
