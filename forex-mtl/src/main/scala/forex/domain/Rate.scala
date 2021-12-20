package forex.domain

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
) {
  def invertRate: Rate =
    Rate(pair.invertPair, price.invertValue, timestamp)
}

object Rate {
  final case class Pair(
      from: Currency,
      to: Currency
  ) {
    def invertPair: Pair = Pair(to, from)
  }
}
