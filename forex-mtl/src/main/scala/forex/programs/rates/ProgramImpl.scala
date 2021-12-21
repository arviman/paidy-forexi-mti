package forex.programs.rates

import cats.data.Validated.{Invalid, Valid}
import cats.effect.IO
import forex.domain._
import forex.services.RatesService
import forex.services.rates.errors

class ProgramImpl(ratesService: RatesService) extends Program {
  def get(request: Protocol.GetRatesRequest): IO[Either[String, Option[Rate]]] = {
    ratesService.get(Rate.Pair(request.from, request.to)) map {
      case Valid(res) => Right(res)
      case Invalid(err: errors.Error) => Left(err.getClass.getSimpleName + " " + err.msg)
    }
  }
}

object ProgramImpl {
  def apply(
      ratesService: RatesService
  ): Program = new ProgramImpl(ratesService)
}
