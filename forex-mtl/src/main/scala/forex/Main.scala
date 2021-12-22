package forex

import cats.effect._
import forex.config._
import forex.domain.{Currency, Rate}
import forex.services.{RateWriterService, RatesServices}
import org.http4s.blaze.server.BlazeServerBuilder
import wvlet.log.Logger



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

class Application {
  def startServer(): IO[ExitCode] = {
    val config = Config.getApplicationConfig("app")
    val wait   = IO.sleep(config.pollDuration)
    val waitOnFailure   = IO.sleep(config.pollOnFailureDuration) // we want to retry sooner in case one-frame-api is down
    for {
      rateMapIO <- Ref.of[IO, Map[Currency, Rate]](Map[Currency, Rate]())
      ratePoller: RateWriterService = RatesServices.ratePollerService(rateMapIO, config.rateApi)
      _ <- (ratePoller.updateRates().flatMap(pollRes => if(pollRes) wait else waitOnFailure)).foreverM.start
      _ <- IO.println("starting server")
      module = new Module(config, rateMapIO)
      code <- BlazeServerBuilder[IO]
               .bindHttp(config.http.port, config.http.host)
               .withHttpApp(module.httpApp)
               .serve
               .compile
               .drain
               .as(ExitCode.Success)

    } yield (code)

  }

}
