package forex

import cats.effect._
import forex.config._
import fs2.Stream
import org.http4s.blaze.server.BlazeServerBuilder

object Main extends IOApp.Simple {
  override def run: IO[Unit] = new Application().stream().compile.drain
}

class Application {

  def stream(): Stream[IO, Unit] =
    for {
      config <- Config.stream("app")
      module = new Module(config)

      _ <- BlazeServerBuilder[IO]
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve

    } yield ()

}
