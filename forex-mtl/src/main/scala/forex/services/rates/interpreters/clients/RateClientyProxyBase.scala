package forex.services.rates.interpreters.clients

import cats.effect.Async
import cats.implicits.toFunctorOps
import forex.config.RateApiConfig
import forex.domain.oneFrameAPI.OneFrameApiResponseRow
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.http.rates.Protocol.OneFrameResponseDecoder
import forex.services.rates.RateClientProxy
import io.lemonlabs.uri.Url
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import wvlet.log.LogSupport

abstract class RateClientyProxyBase[A[_] : Async](config: RateApiConfig) extends RateClientProxy[A] with LogSupport {
  lazy val ratesUrl: Url#Self = Url
    .parse(s"http://${config.host}:${config.port}/rates")
    .addParams(Currency.getListOfCurrencies().filterNot(_.equals("USD")).map(curr => "pair" -> (curr + "USD")))

  type OneFrameApiResponse = List[OneFrameApiResponseRow]
  implicit val oneFrameResponseDec: EntityDecoder[A, OneFrameApiResponse] = jsonOf

  override def getRates: A[List[Rate]] = {

    debug("getting rates")
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
  protected def fetchResponse: A[OneFrameApiResponse]

}


