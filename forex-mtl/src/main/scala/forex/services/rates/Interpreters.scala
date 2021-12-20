package forex.services.rates
import forex.domain.Types.SharedState
import interpreters._

object Interpreters {
  def rateClientProxy = new RateClientProxyDummyImpl()
  def rateService(sharedState: SharedState): RateService =  new RateServiceImpl(sharedState)
  def ratePollerService(sharedStateIO: SharedState): RateWriter = new RateWriterImpl(rateClientProxy, sharedStateIO)
}
