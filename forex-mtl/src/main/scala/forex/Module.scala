package forex

import cats.effect.Temporal
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import org.http4s._
import org.http4s.server.middleware.{AutoSlash, Timeout}
import forex.domain.Types.SharedState
class Module[F[_]](config: ApplicationConfig, rateMapIO: SharedState[F])
                                    (implicit T: Temporal[F]) {

  private val ratesService: RatesService[F] = RatesServices.rateService[F](rateMapIO)

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes(ratesProgram).routes

  private val routesMiddleware: HttpRoutes[F] => HttpRoutes[F] = { http: HttpRoutes[F] =>
    AutoSlash(http)
  }

  private val appMiddleware: HttpApp[F] => HttpApp[F] = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
