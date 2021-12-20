package forex.services.rates
import forex.domain.Types.SharedStateIO
import interpreters._

object Interpreters {
  def rateClientProxy = new RateClientProxyDummyImpl()
  def rateService(sharedState: SharedStateIO): RateService =  new RateServiceImpl(sharedState)
  def ratePollerService(sharedStateIO: SharedStateIO): RateWriter = new RateWriterImpl(rateClientProxy, sharedStateIO)
}
