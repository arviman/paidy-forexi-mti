package forex.services.rates.interpreters

import cats.Applicative
import cats.data.Validated
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.RateService
import forex.services.rates.errors._

class RateServiceDummyImpl[F[_]](implicit A: Applicative[F]) extends RateService[F] {

  override def get(pair: Rate.Pair): F[Validated[Error, Option[Rate]]] =
    A.pure(Validated.valid[Error, Option[Rate]](Some(Rate(pair, Price(BigDecimal(100)), Timestamp.now))))
}
