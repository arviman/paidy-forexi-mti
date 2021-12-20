package forex.services.rates.interpreters

import cats.data.Validated.Valid
import cats.effect.testkit.TestControl
import cats.effect.unsafe.implicits.global
import cats.effect.{ IO, Ref }
import forex.domain.{ Currency, Rate }
import org.scalatest.funsuite.AnyFunSuite

class RateServiceImplTest extends AnyFunSuite {

  test("set cache and test that reading the cache works") {

    val rateClientProxy = new RateClientProxyForTest
    val pair1           = Rate.Pair(Currency("JPY"), Currency("USD"))
    val pair2           = Rate.Pair(Currency("USD"), Currency("JPY"))

    val res = for {
      // init
      rateMap <- Ref.of[IO, Map[Currency, Rate]](Map[Currency, Rate]())
      rateService = new RateServiceImpl(rateMap)
      rates <- rateClientProxy.getRates()
      rateWriter = new RateWriterImpl(rateClientProxy, rateMap)
      _ <- rateWriter.setCache(rates)
      //act
      res1 <- rateService.get(pair1)
      res2 <- rateService.get(pair2)
    } yield (res1, res2, rates)
    //assert
    val ret = TestControl.executeEmbed(res).unsafeRunSync()

    val usdJpyRate = ret._3.find(x => x.pair == pair1).get
    val jpyUsdRate = usdJpyRate.invertRate
    assert(ret._2.equals(Valid(Some(usdJpyRate))))
    assert(ret._1.equals(Valid(Some(jpyUsdRate))))
    // todo fix this test and add a test for a cross pair when it exists
  }
}
