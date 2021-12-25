package forex.services.rates
import cats.Monad
import cats.effect.Async
import forex.config.RateApiConfig
import forex.domain.Types.SharedState
import forex.services.rates.interpreters.clients.RateClientProxyHttpJdkImpl
import interpreters._

object Interpreters {
  def rateClientProxy[F[_] : Async](config: RateApiConfig) = new RateClientProxyHttpJdkImpl[F](config)
  def rateService[G[_] : Monad](sharedState: SharedState[G]): RateService[G] = new RateServiceImpl[G](sharedState)
  def ratePollerService[F[_] : Async](sharedStateIO: SharedState[F], config: RateApiConfig): RateWriter[F] =
    new RateWriterImpl[F](rateClientProxy[F](config), sharedStateIO)
}
