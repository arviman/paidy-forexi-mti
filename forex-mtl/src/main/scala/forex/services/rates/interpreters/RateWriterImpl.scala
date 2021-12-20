package forex.services.rates.interpreters

import cats.effect.{IO, Ref}
import forex.domain.Types.SharedStateIO
import forex.domain.{Currency, Rate}
import forex.services.rates.{RateClientProxy, RateWriter}

class RateWriterImpl(rateClientProxy: RateClientProxy, rateMapIO: SharedStateIO) extends RateWriter {
  override def updateRates(): IO[Unit] = {
    println("setting cache")
    rateClientProxy.getRates().flatMap(setCache)
  }
  // this implementation currently only supports Currency->USD pairs, which is the format we store in our cache
  // it can be extended later to support any from->to combination
  def getMapFromRates(r: List[Rate]): Map[Currency, Rate] =
    r.foldLeft(Map[Currency, Rate]()) { (m, t) => m + (t.pair.from -> t) }

  def setCache(rates: List[Rate]): IO[Unit] = {
    val newMap = getMapFromRates(rates)
    println(s"trying to set new map of size ${newMap.size}")
    rateMapIO.flatMap((rateMap: Ref[IO, Map[Currency, Rate]]) => {
      rateMap.getAndSet(newMap).map((x: Map[Currency, Rate]) => println(s"${x.size} previously existed"))
    })
  }

}

