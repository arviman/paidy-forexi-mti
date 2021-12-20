package forex.services.rates.interpreters
import cats.effect.{IO, Ref}
import cats.data.Validated
import forex.domain.Types.SharedState
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.RateService
import forex.services.rates.errors.Error
import forex.services.rates.errors.Error.CurrencyConversionFailed

// stores all currencies in Currency -> uSD, can be extended later to use Pair as key
class RateServiceImpl(val rateMap: SharedState) extends RateService {

  private def getOldestTime(tm1: Timestamp, tm2: Timestamp): Timestamp = {
    if(tm1.value.compareTo(tm2.value)>0)
      tm2
    else tm1
  }

  private def calculateTransitivePrice(fromPrice: Price, toPrice: Price): Price = {

    // (from -> to) = (from -> USD) / (to -> USD) i.e (from->USD) * (USD->to)
    // if 1 AED is 2 USD, and 1 USD is 5 SGD, then SGD-> USD is 0.2 and USD->SGD=5 (inverse value),so 1 AED = 2 * (5) SGD
    Price(fromPrice.value * toPrice.inverseValue.value)

  }

  private def getRateForPairFromMap(pair: Rate.Pair, rateMap: Ref[IO, Map[Currency, Rate]]): IO[Option[Rate]] = {

    def getRateFromMap(x: Map[Currency, Rate], currency: Currency): Option[Rate] =
      {
        println(s"map contains ${x.size} items")
        for(items <- x)
          println(items._1)
        println(s"checking for $currency")
        if (x.contains(currency)) Some(x(currency)) else None
      }

    for {
      fromRate <- rateMap.get.map { getRateFromMap(_, pair.from) }
      toRate <- rateMap.get.map { getRateFromMap(_, pair.to) }
    } yield (fromRate, toRate) match {
        case (Some(f), Some(t)) =>
          println(s"${f.pair.from} ${f.pair.to} ${t.pair.from} ${t.pair.to}")
          if (pair.to == Currency.USD || pair.from == Currency.USD) {
            if (pair.to == Currency.USD)
              Some(f)
            else
              Some(Rate(pair, t.price.inverseValue, toRate.get.timestamp))
          }
          else { // non-usd to non-usd conversion
            Some(Rate(pair, calculateTransitivePrice(f.price, t.price) , getOldestTime(fromRate.get.timestamp, toRate.get.timestamp)))
          }
        case (Some(_), None) =>
          println("to is null")
          None
        case (None, Some(_)) =>
          println("from is null")
          None
        case _ => {
          println(s"both are null querying for ${pair.from} ${pair.to}")
          None
        }
      }

  }

  override def get(pair: Rate.Pair):IO[Validated[Error, Option[Rate]]] =
    try {
      println("getting rate")
      getRateForPairFromMap(pair, rateMap)
        .map(r=>Validated.valid[Error, Option[Rate]](r))
    }
    catch {
      case _ : ArithmeticException =>
        IO(Validated.invalid[Error, Option[Rate]](CurrencyConversionFailed("Divide by zero Error")))
      case ex : Throwable =>
        IO(Validated.invalid[Error, Option[Rate]](CurrencyConversionFailed("An exception occurred: " + ex.getMessage)))
    }

}