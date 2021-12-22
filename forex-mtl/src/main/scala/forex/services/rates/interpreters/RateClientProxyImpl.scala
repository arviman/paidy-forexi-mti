package forex.services.rates.interpreters

import cats.effect.IO
import forex.config.RateApiConfig
import forex.services.rates.RateClientProxy
import forex.domain.{Currency, Price, Rate, Timestamp}
import org.http4s.Method.GET
import io.lemonlabs.uri.Url
import org.http4s.{Headers, HttpVersion, Request, Uri}
import org.http4s.client.{Client, JavaNetClientBuilder}
import forex.http.rates.Protocol._
import forex.programs.oneFrameAPI.OneFrameApiResponseRow
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

import scala.util.{Failure, Success, Try}

class RateClientProxyImpl(config: RateApiConfig) extends RateClientProxy {
  lazy val ratesUrl = Url
    .parse(s"http://${config.host}:${config.port}/rates")
    .addParams(Currency.getListOfCurrencies().filterNot(_.equals("USD")).map(curr => "pair" -> (curr + "USD")))
  override def getRates(): IO[List[Rate]] =
    fetchResponse.map(
      _.map(
        item =>
          Rate(
            pair = Rate.Pair(item.from, item.to),
            price = Price(item.price),
            timestamp = Timestamp(item.time_stamp)
        )
      ).toList
    )

  /** Decode the response
    * @return an exception
    */
  def fetchResponse: IO[List[OneFrameApiResponseRow]] = {
    println("fetching response from " + ratesUrl.toString)
    Try(
      Uri.fromString(ratesUrl.toString)
        .map(uri =>
          JavaNetClientBuilder[IO].resource.use { // Create a client
              httpClient: Client[IO] =>
                httpClient.expectOr[List[OneFrameApiResponseRow]](Request[IO](GET, uri, HttpVersion.`HTTP/1.1`, Headers(("token") -> config.token)))(
                  err =>
                    IO.println(s"Error while getting response from rate API: ${err.status}") *> IO(new RuntimeException)
                )
          }
        )
    ) match {
      case Success(value) =>
        value match {
          case Left(_) => {
            println("Error creating Uri")
            IO(List[OneFrameApiResponseRow]())
          }
          case Right(res) => {
            res
          }
        }
      case Failure(_) => IO(List[OneFrameApiResponseRow]())
    }
  }

}
