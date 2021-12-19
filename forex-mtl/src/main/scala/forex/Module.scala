package forex

import cats.effect.{IO}
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}

class Module(config: ApplicationConfig) {

  private val ratesService: RatesService = RatesServices.rateService

  private val ratesProgram: RatesProgram = RatesProgram(ratesService, config)

  private val ratesHttpRoutes: HttpRoutes[IO] = new RatesHttpRoutes(ratesProgram).routes

  type PartialMiddleware = HttpRoutes[IO] => HttpRoutes[IO]
  type TotalMiddleware   = HttpApp[IO] => HttpApp[IO]

  private val routesMiddleware: PartialMiddleware = { http: HttpRoutes[IO] => AutoSlash(http)   }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[IO] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[IO] = ratesHttpRoutes

  val httpApp: HttpApp[IO] = appMiddleware(routesMiddleware(http).orNotFound)

}
