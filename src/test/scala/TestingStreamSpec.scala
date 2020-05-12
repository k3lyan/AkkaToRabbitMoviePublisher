import java.nio.file.Paths

import ProductRequirements.{fitWithProductRequirements, getTargetedValue}
import akka.actor.ActorSystem
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.collection.immutable.HashMap
import scala.util.{Failure, Success}

class TestingStreamSpec extends TestKit(ActorSystem("TestingStreamSpec"))
with AnyWordSpecLike
with BeforeAndAfterAll {
  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)
  "A correctly extracted source" should {
    "convert to MapString" in {
      val sourceUnderTest = FileIO.fromPath(Paths.get("src/main/resources/data/title.basics.tsv")).async
        .viaMat(CsvParsing.lineScanner(delimiter = '\t'))(Keep.right)
        .viaMat(CsvToMap.toMapAsStrings())(Keep.left)
      val testSink = TestSink.probe[Map[String, String]]
      val materialized = sourceUnderTest.toMat(testSink)(Keep.right).run()

      val subscriber = materialized
      subscriber
        .request(1)
        .expectNext(HashMap(
          "tconst" -> "tt0000001",
          "titleType" -> "short",
          "primaryTitle" -> "Carmencita",
          "originalTitle" -> "Carmencita",
          "isAdult" -> "0",
          "startYear" -> "1894",
          "endYear" -> "\\N",
          "runtimeMinutes" -> "1",
          "genres" -> "Documentary,Short"
        ))
    }

    "filter to originalTiTle" in {
      val sourceUnderTest = FileIO.fromPath(Paths.get("src/main/resources/data/title.basics.tsv")).async
        .viaMat(CsvParsing.lineScanner(delimiter = '\t'))(Keep.right)
        .viaMat(CsvToMap.toMapAsStrings())(Keep.left)
      val flow = Flow[Map[String, String]]
        .filter(fitWithProductRequirements).async
        .map(m => m("originalTitle")).async

      val testSink = TestSink.probe[String]
      val materialized = sourceUnderTest.via(flow).toMat(testSink)(Keep.right).run()

      val subscriber = materialized
      subscriber
        .request(4)
        .expectNext("Salome Mad", "Jarní sen starého mládence", "Par habitude", "Quincy Adams Sawyer")
    }
  }
}
