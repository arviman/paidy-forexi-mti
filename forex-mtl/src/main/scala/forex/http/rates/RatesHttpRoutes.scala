package forex.http
package rates

import cats.effect.Sync
import cats.effect.std.Console
import cats.syntax.flatMap._
import forex.programs.RatesProgram
import forex.programs.rates.{Protocol => RatesProgramProtocol}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync](rateProgram: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      rateProgram.get(RatesProgramProtocol.GetRatesRequest(from, to)).flatMap {
        case Right(rate) => rate match {
          case Some(r) => Ok(r.asGetApiResponse)
          case None => NoContent()
        }
        case Left(err) => InternalServerError()
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
