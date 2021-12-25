package forex.services.rates
import cats.Monad
import cats.effect.Async
import forex.config.RateApiConfig
import forex.domain.Types.SharedState
import forex.services.rates.interpreters._
import forex.services.rates.interpreters.clients.RateClientProxyHttpJdkImpl

object Interpreters {
  def rateClientProxy[A[_]: Async](config: RateApiConfig)                   = new RateClientProxyHttpJdkImpl[A](config)
  def rateService[M[_]: Monad](sharedState: SharedState[M]): RateService[M] = new RateServiceImpl[M](sharedState)
  def ratePollerService[A[_]: Async](sharedStateIO: SharedState[A], config: RateApiConfig): RateWriter[A] =
    new RateWriterImpl[A](rateClientProxy[A](config), sharedStateIO)
}
