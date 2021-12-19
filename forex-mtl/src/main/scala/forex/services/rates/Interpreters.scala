package forex.services.rates
import interpreters._

object Interpreters {
  def rateService: RateService =  new RateServiceImpl()
}
