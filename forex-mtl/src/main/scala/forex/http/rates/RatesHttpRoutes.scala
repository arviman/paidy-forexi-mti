package forex.http
package rates

import cats.effect.IO
import forex.programs.RatesProgram
import forex.programs.rates.{Protocol => RatesProgramProtocol}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes(rateProgram: RatesProgram) extends Http4sDsl[IO] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      rateProgram.get(RatesProgramProtocol.GetRatesRequest(from, to)).flatMap {
        case Right(rate) => rate match {
          case Some(r) => Ok(r.asGetApiResponse)
          case None => NoContent()
        }
        case Left(_) => InternalServerError() // todo consider exposing error message outside
      }

  }

  val routes: HttpRoutes[IO] = Router(
    prefixPath -> httpRoutes
  )

}
