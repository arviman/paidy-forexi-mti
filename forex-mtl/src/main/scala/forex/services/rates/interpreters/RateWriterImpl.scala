package forex.services.rates.interpreters

import cats.Monad
import cats.implicits.{catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import forex.domain.Types.SharedState
import forex.domain.{Currency, Rate}
import forex.services.rates.{RateClientProxy, RateWriter}
import wvlet.log.LogSupport

// rateclientproxy needs G:Async, while this needs a monad, so let's sandwich F and G into F
class RateWriterImpl[A[_]:Monad](rateClientProxy: RateClientProxy[A], rateMap: SharedState[A]) extends RateWriter[A] with LogSupport {
  /**
   *
   * @return true if 1 or more rates have been added to cache, false if 0 rates were updated
   */

  override def updateRates: A[Boolean] = {
    rateClientProxy.getRates.flatMap(setCache)
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

  def setCache(rates: List[Rate]): A[Boolean] = {
    val newMap: Map[Currency, Rate] = getMapFromRates(rates)
    info(s"Got rates to be added to the cache ${newMap.size}")
    if(newMap.isEmpty)
      false.pure[A]
    else {
      rateMap.updateAndGet(_=>newMap).map(oldMap => info(s"used to have {${oldMap.size}}"))
      newMap.nonEmpty.pure[A]
    }

  }

}
