package forex.services.rates.interpreters

import cats.{Applicative, Functor, Monad}
import cats.data.Validated
import cats.effect.{Concurrent, Ref}
import cats.implicits.{catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.RateService
import forex.services.rates.errors.Error
import forex.services.rates.errors.Error.CurrencyConversionFailed

import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}
/*import org.http4s.Method.GET
import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.client.blaze._*/
// stores all currencies in Currency -> uSD, can be extended later to use Pair as key
class RateServiceImpl[F[_] : Applicative : Concurrent : Monad](val rateMap: F[Ref[F, Map[Currency, Rate]]]) extends RateService[F] {


  private def getOldestTime(tm1: Timestamp, tm2: Timestamp): Timestamp = {
    if(tm1.value.compareTo(tm2.value)>0)
      tm2
    else tm1
  }

  private def calculateTransitivePrice(m: Map[Currency, Rate], from: Currency, to: Currency): Price = {
    val fromInUsd = m(from).price
    val toInUsd = m(to).price
    // (from -> to) = (from -> USD) / (to -> USD) i.e (from->USD) * (USD->to)
    // if 1 AED is 2 USD, and 1 USD is 5 SGD, then SGD-> USD is 0.2 and USD->SGD=5 (inverse value),so 1 AED = 2 * (5) SGD
    Price(fromInUsd.value * toInUsd.inverseValue.value)
  }

  private def getRateForPairFromMap(pair: Rate.Pair, rmap: Ref[F, Map[Currency, Rate]]): F[Rate] = {
    if (pair.to == Currency.USD || pair.from == Currency.USD) {
      if (pair.to == Currency.USD)
        rmap.get.map {
          _ (pair.from)
        }
      else
        rmap.get.map { m => Rate(pair, m(pair.to).price.inverseValue, m(pair.to).timestamp) }
    }
    else { // non-usd to non-usd conversion
      rmap.get.map(m => Rate(pair, calculateTransitivePrice(m, pair.from, pair.to), getOldestTime(m(pair.from).timestamp, m(pair.to).timestamp)))
    }
  }

  override def get(pair: Rate.Pair):F[Validated[Error, Rate]] =
    try {
      rateMap.flatMap(getRateForPairFromMap(pair, _))
        .map(Validated.valid[Error, Rate])
    }
    catch {
      case _ : ArithmeticException =>
        Validated.invalid[Error, Rate](CurrencyConversionFailed("Divide by zero Error")).pure[F]
      case ex =>
        Validated.invalid[Error, Rate](CurrencyConversionFailed("An exception occurred: " + ex.getMessage)).pure[F]
    }


  /*
  private def fetchFromService() = {
    import org.http4s.Uri.uri
    import cats.effect.IO
    import io.circe.generic.auto._
    import fs2.Stream

    // Decode the response
    def fetch(name: String): Stream[IO, Hello] = {
      // Encode a User request
      // todo change from config
      val req = GET(uri("http://localhost:8080/"), User(name).asJson)
      // Create a client
      BlazeClientBuilder[IO](ec).stream.flatMap { httpClient =>
        // Decode a Hello response
        Stream.eval(httpClient.expect(req)(jsonOf[IO, Hello]))
      }
    }
  }

   */



  override def poll(duration: FiniteDuration): Unit = ???
}