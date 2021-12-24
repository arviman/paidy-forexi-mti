package forex

package object services {
  type RatesService[F[_]] = rates.RateService[F]
  type RateWriterService[F[_]] = rates.RateWriter[F]
  final val RatesServices = rates.Interpreters
}
