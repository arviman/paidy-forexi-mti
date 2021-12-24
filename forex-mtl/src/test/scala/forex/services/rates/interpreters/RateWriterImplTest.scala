package forex.services.rates.interpreters

import cats.effect.testkit.TestControl
import cats.effect.unsafe.implicits.global
import cats.effect.{ IO, Ref }
import forex.domain.{ Currency, Rate }
import org.scalatest.funsuite.AnyFunSuite

class RateWriterImplTest extends AnyFunSuite {
  test("get map of returned 3 returns map of size 3") {
    val rateClientProxy = new RateClientProxyForTest[IO]

    val res: IO[(Int, Int)] = for {
      rateMap <- Ref.of[IO, Map[Currency, Rate]](Map[Currency, Rate]())
      rates <- rateClientProxy.getRates
      p = new RateWriterImpl(rateClientProxy, rateMap)
      m = p.getMapFromRates(rates)
    } yield (m.size, rates.size)

    val ret = TestControl.executeEmbed(res).unsafeRunSync()

    assert(ret._1 == ret._2) //map's size must be same as list

    assert(ret._2 == 3) // list must be 3

  }
}
