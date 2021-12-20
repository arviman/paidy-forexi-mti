package forex.services.rates

import cats.effect.{IO}

trait RateWriter {
  def updateRates():IO[Unit]

}
