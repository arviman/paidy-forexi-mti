package forex

import cats.effect.IO
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{ AutoSlash, Timeout }
import forex.domain.Types.SharedState
class Module(config: ApplicationConfig, rateMapIO: SharedState) {

  private val ratesService: RatesService = RatesServices.rateService(rateMapIO)

  private val ratesProgram: RatesProgram = RatesProgram(ratesService)

  private val ratesHttpRoutes: HttpRoutes[IO] = new RatesHttpRoutes(ratesProgram).routes

  private val routesMiddleware: HttpRoutes[IO] => HttpRoutes[IO] = { http: HttpRoutes[IO] =>
    AutoSlash(http)
  }

  private val appMiddleware: HttpApp[IO] => HttpApp[IO] = { http: HttpApp[IO] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[IO] = ratesHttpRoutes

  val httpApp: HttpApp[IO] = appMiddleware(routesMiddleware(http).orNotFound)

}
