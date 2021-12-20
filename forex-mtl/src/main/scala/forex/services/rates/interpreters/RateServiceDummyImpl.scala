package forex.services.rates.interpreters

import forex.services.rates.RateService
import cats.data.Validated
import cats.effect.IO
import forex.domain.{ Price, Rate, Timestamp }
import forex.services.rates.errors._

class RateServiceDummyImpl extends RateService {

  override def get(pair: Rate.Pair): IO[Validated[Error, Option[Rate]]] =
    IO(Validated.valid[Error, Option[Rate]](Some(Rate(pair, Price(BigDecimal(100)), Timestamp.now))))
}
