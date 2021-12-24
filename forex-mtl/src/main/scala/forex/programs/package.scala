package forex

package object programs {
  type RatesProgram[F[_]] = rates.Program[F]
  final val RatesProgram = rates.ProgramImpl
}
