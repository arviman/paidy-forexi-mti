package forex.services.rates

import cats.data.Validated
import forex.domain.Rate
import errors._

import scala.concurrent.duration.FiniteDuration

trait RateService[F[_]] {
  def get(pair: Rate.Pair): F[Validated[Error, Rate]]
  def poll(duration: FiniteDuration): Unit
}
