package forex

import cats.effect._
import forex.config._
import forex.domain.{Currency, Rate}
import forex.services.RatesServices
import wvlet.log.{LogSupport, Logger}

object Main extends IOApp {
  def setLogConfig(): Unit = {
    Logger.clearAllHandlers
    Logger.setDefaultFormatter(ForexMtiLogFormatter)
  }
  override def run(args: List[String]): IO[ExitCode] = {
    setLogConfig()
    new Application()
      .startServer()
  }
}

class Application extends LogSupport{
  def startServer(): IO[ExitCode] = {
    val config = Config.getApplicationConfig("app")
    val wait: IO[Unit] = IO.sleep(config.pollDuration)
    val waitOnFailure: IO[Unit] = IO.sleep(config.pollOnFailureDuration) // we want to retry sooner in case one-frame-api is down

    for {
      rateMap <- Ref.of[IO, Map[Currency, Rate]](Map[Currency, Rate]())
      _ = info("starting server")
      _ <- (RatesServices.ratePollerService[IO](rateMap, config.rateApi)
        .updateRates flatMap (if(_) wait else waitOnFailure)).foreverM.start
      module: Module[IO] = new Module[IO](config, rateMap)
      code <- org.http4s.blaze.server.BlazeServerBuilder[IO]
               .bindHttp(config.http.port, config.http.host)
               .withHttpApp(module.httpApp)
               .serve
               .compile
               .drain
               .as(ExitCode.Success)

    } yield (code)

  }

}
