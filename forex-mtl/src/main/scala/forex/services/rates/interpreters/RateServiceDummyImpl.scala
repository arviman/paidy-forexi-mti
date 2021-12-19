package forex.services.rates.interpreters

import forex.services.rates.RateService
import cats.data.Validated
import cats.effect.IO
import cats.syntax.applicative._
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.errors._

import scala.concurrent.duration.FiniteDuration

class RateServiceDummyImpl extends RateService {

  override def get(pair: Rate.Pair): IO[Validated[Error, Option[Rate]]] =
    Validated.valid[Error,Option[Rate]](Some(Rate(pair, Price(BigDecimal(100)), Timestamp.now))).pure[IO]


  override def poll(duration: FiniteDuration): Unit = {  }
}
