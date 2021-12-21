package forex.services.rates.interpreters

import cats.effect.IO
import forex.config.RateApiConfig
import forex.services.rates.RateClientProxy
import forex.domain.{Currency, Price, Rate}
import forex.programs.oneFrameAPI.OneFrameApiResponse
import org.http4s.Method.GET
import org.http4s.blaze.client.BlazeClientBuilder
import io.lemonlabs.uri.Url
import org.http4s.{Headers, HttpVersion, Request, Uri}
import org.http4s.client.Client
import forex.http.rates.Protocol._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

import scala.util.{Failure, Success, Try}

class RateClientProxyImpl(config: RateApiConfig) extends RateClientProxy {
  lazy val ratesUrl = Url.parse(s"${config.baseUri}:${config.port}/rates").addParams(Currency.getListOfOtherCurrencies().map("pair" -> _))
  override def getRates(): IO[List[Rate]] =
      fetchResponse.map(
          r => r.arr.map(item => Rate(
            pair = Rate.Pair(item.from, item.to),
            price = Price(item.price),
            timestamp = item.time_stamp
          )).toList
        )
  /** Decode the response
   * @return an exception
   */
  def fetchResponse: IO[OneFrameApiResponse] = {

    Try(
      Uri.fromString(ratesUrl.toString)
        .map(uri => Request[IO](GET, uri, HttpVersion.`HTTP/1.1`, Headers(("token") -> config.token)))
        .map(req => BlazeClientBuilder[IO].resource.use { // Create a client
          httpClient: Client[IO] =>
            httpClient.expectOr[OneFrameApiResponse](req)(err =>
              IO(println(s"Error while getting response from rate API: ${err.status}")) *> IO(new RuntimeException))
        })
      )
    match {
      case Success(value) => value match {
        case Left(_) => IO(println("Error creating Uri")) *> IO(OneFrameApiResponse())
        case Right(res) => res
      }
      case Failure(_) => IO(OneFrameApiResponse())
    }
  }


}


