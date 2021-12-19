package forex.programs.rates

import cats.effect.IO
import forex.domain.Rate

trait Program {
  def get(request: Protocol.GetRatesRequest): IO[String Either Option[Rate]]
}
