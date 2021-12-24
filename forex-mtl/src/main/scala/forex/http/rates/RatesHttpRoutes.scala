package forex.http
package rates

import cats.Monad
import cats.implicits.toFlatMapOps
import forex.programs.RatesProgram
import forex.programs.rates.{Protocol => RatesProgramProtocol}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_] : Monad](rateProgram: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  // this is the end of the world in this context -> F[Response[F]]
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      rateProgram.get(RatesProgramProtocol.GetRatesRequest(from, to)).flatMap {
        case Right(rate) => rate match {
          case Some(r) => Ok(r.asGetApiResponse)
          case None => NoContent()
        }
        case Left(_) => InternalServerError() // todo consider exposing error message outside
      }

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
