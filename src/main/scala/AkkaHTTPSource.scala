import AkkaToRabbit.system
import ProductRequirements.{fitWithProductRequirements, getTargetedValue}
import akka.NotUsed
import akka.http.scaladsl._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.Success
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode, StatusCodes}
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Compression, Source}
import akka.util.ByteString

object AkkaHTTPSource {

  val httpRequest = HttpRequest(uri = "https://datasets.imdbws.com/title.basics.tsv.gz")

  private def extractContent(response: HttpResponse): Source[ByteString, _] =
    response match {
      case HttpResponse(StatusCodes.OK, _, entity, _) => entity.dataBytes
      case notOkResponse =>
        Source.failed(new RuntimeException(s"Error while retrieving data: $notOkResponse"))
    }

  private val sourceHTTP = Source
    .single(httpRequest)
    .mapAsync(4)(Http()(system).singleRequest(_))
    .flatMapConcat {
     extractContent
    }
    .via(Compression.gunzip())

  val filteredHTTPSource = sourceHTTP
    .via(CsvParsing.lineScanner(delimiter='\t')).async
    .via(CsvToMap.toMapAsStrings()).async
    .filter(fitWithProductRequirements).async
    .map(getTargetedValue).async
}


