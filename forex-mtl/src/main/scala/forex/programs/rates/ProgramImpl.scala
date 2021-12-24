package forex.programs.rates

import cats.Functor
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.toFunctorOps
import forex.domain._
import forex.services.RatesService
import forex.services.rates.errors
import wvlet.log.LogSupport

class ProgramImpl[F[_] : Functor](ratesService: RatesService[F]) extends Program[F] with LogSupport {
  def get(request: Protocol.GetRatesRequest): F[Either[String, Option[Rate]]] = {
    ratesService.get(Rate.Pair(request.from, request.to)).map {
      case Valid(res) => {
        info(s"${request.from}-${request.to}" + (if(res.isDefined) "found rate" else "not found"))
        Right(res)
      }
      case Invalid(err: errors.Error) => {
        error(err.msg)
        Left(err.getClass.getSimpleName + " " + err.msg)
      }
    }
  }
}

object ProgramImpl{
  def apply[F[_] : Functor] (ratesService: RatesService[F]): Program[F] = new ProgramImpl[F](ratesService)
}
