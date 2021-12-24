package forex.services.rates

trait RateWriter[F[_]] {
  def updateRates:F[Boolean]
}
