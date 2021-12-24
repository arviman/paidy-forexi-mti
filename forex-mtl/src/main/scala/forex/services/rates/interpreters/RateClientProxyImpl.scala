package forex.services.rates.interpreters

import cats.effect.Async
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import forex.config.RateApiConfig
import forex.domain.oneFrameAPI.OneFrameApiResponseRow
import forex.services.rates.RateClientProxy
import forex.domain.{Currency, Price, Rate, Timestamp}
import org.http4s.Method.GET
import io.lemonlabs.uri.Url
//import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe.jsonOf
import org.http4s.{EntityDecoder, Headers, HttpVersion, Request, Uri}
import org.http4s.client.{Client, JavaNetClientBuilder}
import wvlet.log.LogSupport

import scala.util.{Failure, Success, Try}

class RateClientProxyImpl[A[_] : Async](config: RateApiConfig) extends RateClientProxy[A] with LogSupport {
  type OneFrameApiResponse = List[OneFrameApiResponseRow]
  lazy val ratesUrl = Url
    .parse(s"http://${config.host}:${config.port}/rates")
    .addParams(Currency.getListOfCurrencies().filterNot(_.equals("USD")).map(curr => "pair" -> (curr + "USD")))
  override def getRates: A[List[Rate]] = {
    fetchResponse.map(
      _.map(
        item =>
          Rate(
            pair = Rate.Pair(Currency(item.from), Currency(item.to)),
            price = Price(item.price),
            timestamp = Timestamp(item.time_stamp)
        )
      ).toList
    )
  }


  /** Decode the response
    * @return an exception
    */
  def fetchResponse: A[OneFrameApiResponse] = {
    info("fetching response from " + ratesUrl.toString)
    //import forex.http.rates.Protocol.currencyDecoder
    import forex.http.rates.Protocol.OneFrameResponseDecoder
    //implicit val currencyDec: EntityDecoder[A, Currency] = jsonOf
    implicit val oneFrameResponseDec: EntityDecoder[A, OneFrameApiResponse] = jsonOf

    Try(
      Uri.fromString(ratesUrl.toString)
        .map(uri =>
          JavaNetClientBuilder[A].resource.use { // Create a client
              httpClient: Client[A] =>
                httpClient.expectOr[List[OneFrameApiResponseRow]](Request[A](GET, uri, HttpVersion.`HTTP/1.1`, Headers(("token") -> config.token)))(
                  err => {
                    error(s"Error while getting response from rate API: ${err.status}")
                    (new Throwable).pure[A]
                  }
                )
          }
        )
    ) match {
      case Success(value) =>
        value match {
          case Left(_) => {
            error("Error creating Uri")
            (List[OneFrameApiResponseRow]()).pure[A]
          }
          case Right(res) => {
            res.map(x => info(s"Returning response from API ${x.size}"))
            res
          }
        }
      case Failure(err) => {
        error(err.getMessage)
        (List[OneFrameApiResponseRow]()).pure[A]
      }
    }
  }

}
