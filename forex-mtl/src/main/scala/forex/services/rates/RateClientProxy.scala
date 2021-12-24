package forex.services.rates

import forex.domain.Rate

trait RateClientProxy[A[_]] {
  def getRates: A[List[Rate]]
}
