package forex.services.rates.interpreters

import cats.effect.IO
import forex.domain.Types.SharedState
import forex.domain.{ Currency, Rate }
import forex.services.rates.{ RateClientProxy, RateWriter }

class RateWriterImpl(rateClientProxy: RateClientProxy, rateMap: SharedState) extends RateWriter {
  /**
   *
   * @return true if 1 or more rates have been added to cache, false if 0 rates were updated
   */

  override def updateRates(): IO[Boolean] = {
    rateClientProxy.getRates().flatMap(setCache)
  }

  /**
  // stores all currencies in Currency -> USD form, can be extended later to use Pair as key to support cross pair rates directly
  // this implementation currently only supports USD->Currency or Currency->USD pairs, which is the format we store in our cache
  // it can be extended later to support any from->to combination
    */
  def getMapFromRates(r: List[Rate]): Map[Currency, Rate] = {
    val currencyUSD = Currency("USD")
    r.foldLeft(Map[Currency, Rate]().empty) { (m, t) =>
      if (t.pair.from == currencyUSD)
        m + (t.pair.to -> t.invertRate)
      else if (t.pair.to == currencyUSD)
        m + (t.pair.from -> t)
      else m
    }
  }

  def setCache(rates: List[Rate]): IO[Boolean] = {
    val newMap = getMapFromRates(rates)
    if(newMap.isEmpty)
      IO(false)
    else
      rateMap.set(newMap) >> rateMap.get.map(_.nonEmpty)
  }

}
