package forex.services.rates

object errors {

  sealed abstract class Error (val msg: String)

  object Error {
    final case class OneFrameLookupFailed(override val msg: String) extends Error(msg)
    final case class CurrencyConversionFailed(override val msg: String) extends Error(msg)
  }

}
