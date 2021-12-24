package forex.services.rates
import forex.config.RateApiConfig
import forex.domain.Types.SharedState
import interpreters._

object Interpreters {
  def rateClientProxy(config: RateApiConfig)                = new RateClientProxyImpl(config)
  def rateService(sharedState: SharedState): RateService        = new RateServiceImpl(sharedState)
  def ratePollerService(sharedStateIO: SharedState, config: RateApiConfig): RateWriter = new RateWriterImpl(rateClientProxy(config), sharedStateIO)
}
