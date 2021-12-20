package forex.services.rates.interpreters

import cats.effect.IO
import forex.domain.Types.SharedState
import forex.domain.{Currency, Rate}
import forex.services.rates.{RateClientProxy, RateWriter}


class RateWriterImpl(rateClientProxy: RateClientProxy, rateMap: SharedState) extends RateWriter {
  override def updateRates(): IO[Unit] = {
    println("setting cache")
    rateClientProxy.getRates().flatMap(setCache)
  }
  // this implementation currently only supports USD->Currency or Currency->USD pairs, which is the format we store in our cache
  // it can be extended later to support any from->to combination
  def getMapFromRates(r: List[Rate]): Map[Currency, Rate] =
  {
    val currencyUSD = Currency("USD")
    r.foldLeft(Map[Currency, Rate]().empty) { (m, t) =>
      if (t.pair.from == currencyUSD)
        m +(t.pair.to -> t)
      else if (t.pair.to == currencyUSD)
        m + (t.pair.from -> t)
      else m
    }
  }

  def setCache(rates: List[Rate]): IO[Unit] = {
    val newMap = getMapFromRates(rates)
    println(s"trying to set new map of size ${newMap.size} with ${rates.size}")
    rateMap.getAndSet(newMap)
      .map((x: Map[Currency, Rate]) => println(s"${x.size} previously existed"))
  }

}

