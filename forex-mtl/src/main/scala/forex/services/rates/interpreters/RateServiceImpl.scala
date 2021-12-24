package forex.services.rates.interpreters
import cats.Monad
import cats.effect.Ref
import cats.data.Validated
import cats.implicits.{catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import forex.domain.Types.SharedState
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.RateService
import forex.services.rates.errors.Error
import forex.services.rates.errors.Error.CurrencyConversionFailed

class RateServiceImpl[F[_] : Monad](val rateMap: SharedState[F]) extends RateService[F] {

  private def getOldestTime(tm1: Timestamp, tm2: Timestamp): Timestamp =
    if (tm1.value.compareTo(tm2.value) > 0)
      tm2
    else tm1

  private def calculateTransitivePrice(fromPrice: Price, toPrice: Price): Price =
    // (from -> to) = (from -> USD) / (to -> USD) i.e (from->USD) * (USD->to)
    // if 1 AED is 2 USD, and 1 USD is 5 SGD, then SGD-> USD is 0.2 and USD->SGD=5 (inverse value),so 1 AED = 2 * (5) SGD
    Price(fromPrice.value * toPrice.invertValue.value)

  private def getRateForPairFromMap(queryPair: Rate.Pair, rateMap: Ref[F, Map[Currency, Rate]]): F[Option[Rate]] = {

    def getRateFromMap(x: Map[Currency, Rate], currency: Currency): Option[Rate] = {
      if (x.contains(currency)) Some(x(currency)) else None
    }
    def getInCurrencyToUSDForm(queryPair: Rate.Pair): Rate.Pair =
      if (queryPair.from == Currency.USD)
        queryPair.invertPair
      else queryPair
    val pair: Rate.Pair = getInCurrencyToUSDForm(queryPair)

    val frr: F[Option[Rate]] = rateMap.get.map { getRateFromMap(_, pair.from) }
    val trr: F[Option[Rate]] = rateMap.get.map { getRateFromMap(_, pair.to) }

    frr flatMap (fromRate => trr map(toRate =>
      (fromRate, toRate) match {
        case (Some(f), Some(t)) =>
          if (pair.to == Currency.USD || pair.from == Currency.USD) {
            if (queryPair.to == Currency.USD) // xyzUSD
              Some(f)
            else // USDxyz
              Some(f.invertRate) // obtained rate was for an inverted pair, so re-invert rate
          } else { // cross pair(non-usd to non-usd) conversion
            Some(
              Rate(
                pair,
                calculateTransitivePrice(f.price, t.price),
                getOldestTime(fromRate.get.timestamp, toRate.get.timestamp)
              )
            )
          }
        case (Some(f), None) =>
          if (queryPair.to == Currency.USD && queryPair.from != Currency.USD) // xyzUSD
            Some(f)
          else if(queryPair.to != Currency.USD && queryPair.from == Currency.USD)
            Some(f.invertRate)
          else
            None
        case (None, Some(t)) =>
          if (queryPair.from == Currency.USD && queryPair.to != Currency.USD) // USDxyz
            Some(t.invertRate) // t is abcUSD, so invert it
          else
            None
        case _ =>
          None
      }))

  }

  override def get(pair: Rate.Pair): F[Validated[Error, Option[Rate]]] =
    try {
      getRateForPairFromMap(pair, rateMap)
        .map(r => Validated.valid[Error, Option[Rate]](r))
    } catch {
      case _: ArithmeticException =>
        (Validated.invalid[Error, Option[Rate]](CurrencyConversionFailed("Divide by zero Error"))).pure[F]
      case ex: Throwable =>
        (Validated.invalid[Error, Option[Rate]](CurrencyConversionFailed("An exception occurred: " + ex.getMessage))).pure[F]
    }

}
