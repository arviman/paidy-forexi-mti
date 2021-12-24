package forex.services.rates.interpreters

import cats.effect.IO
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.services.rates.RateClientProxy

import java.time.OffsetDateTime
import scala.util.Random

class RateClientProxyDummyImpl extends RateClientProxy {
  override def getRates(): IO[List[Rate]] =
    IO(
      List(
        Rate(
          pair = Rate.Pair(Currency("USD"), Currency("JPY")),
          price = Price(Random.nextInt(110) + 1),
          timestamp = Timestamp(OffsetDateTime.now())
        ),
        Rate(
          pair = Rate.Pair(Currency("USD"), Currency("SGD")),
          price = Price(Random.nextInt(2) + 1),
          timestamp = Timestamp(OffsetDateTime.now())
        ),
        Rate(
          pair = Rate.Pair(Currency("USD"), Currency("GBP")),
          price = Price(Random.nextInt(3) + 1),
          timestamp = Timestamp(OffsetDateTime.now())
        )
      )
    )
}
