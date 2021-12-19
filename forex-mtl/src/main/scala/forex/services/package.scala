package forex

package object services {
  type RatesService[F[_]] = rates.RateService[F]
  final val RatesServices = rates.Interpreters
}
