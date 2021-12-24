package forex.services.rates

import cats.effect.IO
import forex.domain.Rate

trait RateClientProxy {
  def getRates(): IO[List[Rate]]
}
