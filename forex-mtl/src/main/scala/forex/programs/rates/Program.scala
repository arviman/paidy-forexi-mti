package forex.programs.rates

import forex.domain.Rate

trait Program[F[_]] {
  def get(request: Protocol.GetRatesRequest): F[String Either Option[Rate]]
}
