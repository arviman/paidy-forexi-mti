package forex

import cats.implicits.{catsSyntaxFlatMapOps, toFlatMapOps, toFunctorOps}
import cats.effect._
import cats.effect.implicits.genSpawnOps
import forex.config._
import forex.domain.{Currency, Rate}
import forex.services.{RateWriterService, RatesServices}
import wvlet.log.{LogSupport, Logger}

object Main extends IOApp {
  def setLogConfig(): Unit = {
    Logger.clearAllHandlers
    Logger.setDefaultFormatter(ForexMtiLogFormatter)
  }
  override def run(args: List[String]): IO[ExitCode] = {
    setLogConfig()
    new Application[IO]()
      .startServer()
  }
}

class Application[F[_]](implicit A:Async[F]) extends LogSupport{
  def startServer(): F[ExitCode] = {
    val config = Config.getApplicationConfig("app")
    val wait: F[Unit] = Temporal[F].sleep(config.pollDuration)
    val waitOnFailure: F[Unit] = Temporal[F].sleep(config.pollOnFailureDuration) // we want to retry sooner in case one-frame-api is down

    for {
      rateMap <- Ref.of[F, Map[Currency, Rate]](Map[Currency, Rate]())
      ratePoller: RateWriterService[F] = RatesServices.ratePollerService[F](rateMap, config.rateApi)
      _: Fiber[F, Throwable, Unit] = ratePoller.updateRates.map(res => if(res) wait else waitOnFailure).foreverM.start
      _ = info("starting server")
      module = new Module(config, rateMap)
      code <- org.http4s.blaze.server.BlazeServerBuilder[F]
               .bindHttp(config.http.port, config.http.host)
               .withHttpApp(module.httpApp)
               .serve
               .compile
               .drain
               .as(ExitCode.Success)

    } yield (code)

  }

}
