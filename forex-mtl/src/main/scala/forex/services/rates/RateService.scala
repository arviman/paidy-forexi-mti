package forex.services.rates

import cats.data.Validated
import cats.effect.IO
import forex.domain.Rate
import errors._

trait RateService {
  def get(pair: Rate.Pair): IO[Validated[Error, Option[Rate]]]
}
