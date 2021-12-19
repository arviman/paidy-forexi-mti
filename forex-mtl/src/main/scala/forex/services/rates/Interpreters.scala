package forex.services.rates

import cats.Applicative
import cats.effect.Ref
import cats.effect.kernel.Concurrent
import cats.implicits.catsSyntaxApplicativeId
import forex.domain.{Currency, Price, Rate}
import interpreters._

object Interpreters {
  def rateService[F[_]: Applicative : Concurrent]: RateService[F] =
    new RateServiceImpl[F](Ref.of[F, Map[Currency, Rate]](Map[Currency, Rate]()))
}
