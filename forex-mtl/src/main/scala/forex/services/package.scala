package forex

package object services {
  type RatesService = rates.RateService
  type RateWriterService = rates.RateWriter
  final val RatesServices = rates.Interpreters
}
