package forex

import cats.effect._
import forex.config._
import forex.domain.{ Currency, Rate }
import forex.services.{ RateWriterService, RatesServices }
import org.http4s.blaze.server.BlazeServerBuilder

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    new Application()
      .startServer()
}

class Application {
  def startServer(): IO[ExitCode] = {
    val config = Config.getApplicationConfig("app")
    val wait   = IO.sleep(config.pollDuration)
    for {
      rateMapIO <- Ref.of[IO, Map[Currency, Rate]](Map[Currency, Rate]())
      ratePoller: RateWriterService = RatesServices.ratePollerService(rateMapIO)
      _ <- (ratePoller.updateRates() <* wait).foreverM.start
      _ <- IO(println("starting server"))
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
