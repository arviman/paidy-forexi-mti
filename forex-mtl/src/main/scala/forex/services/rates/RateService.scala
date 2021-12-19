package forex.services.rates

import cats.data.Validated
import cats.effect.IO
import forex.domain.Rate
import errors._

import scala.concurrent.duration.FiniteDuration

trait RateService {
  def get(pair: Rate.Pair): IO[Validated[Error, Option[Rate]]]
  def poll(duration: FiniteDuration): Unit
}
