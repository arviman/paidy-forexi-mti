package forex.services.rates.interpreters

import cats.data.Validated
import cats.data.Validated.Valid
import cats.effect.testkit.TestControl
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref}
import forex.domain.{Currency, Rate}
import forex.services.rates.errors
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite

class RateServiceImplTest extends AnyFunSuite {

  test("set cache and test that reading the cache works") {

    val rateClientProxy = new RateClientProxyForTest[IO]
    val pair1           = Rate.Pair(Currency("JPY"), Currency("USD"))
    val pair2           = Rate.Pair(Currency("USD"), Currency("JPY"))

    val res = for {
      // init
      rateMap <- Ref.of[IO, Map[Currency, Rate]](Map[Currency, Rate]())
      rateService = new RateServiceImpl(rateMap)
      rates <- rateClientProxy.getRates
      rateWriter = new RateWriterImpl(rateClientProxy, rateMap)
      _ <- rateWriter.setRates(rates)
      //act
      res1 <- rateService.get(pair1)
      res2 <- rateService.get(pair2)
    } yield (res1, res2, rates)
    //assert
    val ret: (Validated[errors.Error, Option[Rate]], Validated[errors.Error, Option[Rate]], List[Rate]) =
      TestControl.executeEmbed(res).unsafeRunSync()

    val usdJpyRate = ret._3.find(x => x.pair == pair2).get
    val jpyUsdRate = usdJpyRate.invertRate

    assertValidRate(ret._1, jpyUsdRate)
    assertValidRate(ret._2, usdJpyRate)
  }

  val EPSILON = BigDecimal(1/10000000.0)
  def assertRate(value: Rate, rate: Rate): Assertion = {
    assert(value.pair.equals(rate.pair))
    assert(value.timestamp.equals(rate.timestamp))
    assert((value.price.value - rate.price.value) <= (EPSILON))
  }

  def assertValidRate(value: Validated[errors.Error, Option[Rate]], rate: Rate) = {

    assert(value.isValid)
    value.foreach(
      (rt: Option[Rate]) => {
        assert(rt.isDefined)
        val _ = assertRate(rt.get, rate)
      }

    )

  }

  test("set cache and test cross pair") {

    val rateClientProxy = new RateClientProxyForTest[IO]
    val pair1           = Rate.Pair(Currency("JPY"), Currency("SGD"))
    val pair2           = Rate.Pair(Currency("SGD"), Currency("JPY"))

    val res = for {
      // init
      rateMap <- Ref.of[IO, Map[Currency, Rate]](Map[Currency, Rate]())
      rateService = new RateServiceImpl(rateMap)
      rates <- rateClientProxy.getRates
      rateWriter = new RateWriterImpl(rateClientProxy, rateMap)
      _ <- rateWriter.setRates(rates)
      //act
      res1 <- rateService.get(pair1)
      res2 <- rateService.get(pair2)
    } yield (res1, res2, rates)
    //assert
    val ret: (Validated[errors.Error, Option[Rate]], Validated[errors.Error, Option[Rate]], List[Rate]) =
      TestControl.executeEmbed(res).unsafeRunSync()

    // assert that JPYSGD's inverted price is SGDJPY's price
    assert(ret._1 match {
      case Valid(a) => a.isDefined && (ret._2 match {
        case Valid(b) => b.isDefined && b.get.price.value != BigDecimal.valueOf(0) && b.exists(x => x.invertRate.equals(a.get))
        case _ => false
      })
      case Validated.Invalid(_) => false
    })
  }
}
