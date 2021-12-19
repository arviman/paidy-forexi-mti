package forex.config

import cats.effect.{IO, Sync}
import fs2.Stream
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Config {

  /**
   * @param path the property path inside the default configuration
   */
  def stream(path: String): Stream[IO, ApplicationConfig] = {
    Stream.eval(Sync[IO].delay(
      ConfigSource.default.at(path).loadOrThrow[ApplicationConfig]))
  }

}
