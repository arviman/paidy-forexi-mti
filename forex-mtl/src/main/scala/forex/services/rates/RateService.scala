package forex.services.rates

import cats.data.Validated
import forex.domain.Rate
import errors._

trait RateService[F[_]] {
  def get(pair: Rate.Pair): F[Validated[Error, Option[Rate]]]
}
