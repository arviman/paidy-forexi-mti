package forex.programs.rates

import cats.data.Validated.{Invalid, Valid}
import cats.{Applicative}
import cats.effect.Async
import cats.implicits.toFunctorOps
import forex.config.ApplicationConfig
import forex.domain._
import forex.services.RatesService
import forex.services.rates.errors

class Program[F[_]: Applicative](
    ratesService: RatesService[F]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[String Either Rate] = {
    ratesService.get(Rate.Pair(request.from, request.to)).map {
      case Valid(res) => Right(res)
      case Invalid(err: errors.Error) => Left(err.getClass.getSimpleName + " " + err.msg)
    }
  }

}

object Program {

  def apply[F[_]: Applicative : Async](
      ratesService: RatesService[F], config: ApplicationConfig
  ): Algebra[F] = new Program[F](ratesService) {
    ratesService.poll(config.pollDuration)
  }

}
