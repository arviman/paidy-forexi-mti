package forex.services.rates.interpreters

import forex.services.rates.RateService
import cats.Applicative
import cats.data.Validated
import cats.syntax.applicative._
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.errors._

import scala.concurrent.duration.FiniteDuration

class RateServiceDummyImpl[F[_]: Applicative] extends RateService[F] {

  override def get(pair: Rate.Pair): F[Validated[Error, Rate]] =
    Validated.valid[Error,Rate](Rate(pair, Price(BigDecimal(100)), Timestamp.now)).pure[F]


  override def poll(duration: FiniteDuration): Unit = {  }
}
