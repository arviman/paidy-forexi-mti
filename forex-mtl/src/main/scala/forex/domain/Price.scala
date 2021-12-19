package forex.domain
import scala.math.BigDecimal
case class Price(value: BigDecimal) extends AnyVal {
  @throws(classOf[ArithmeticException])
  def inverseValue: Price = Price( BigDecimal.valueOf(1) / value)
}

object Price {
  def apply(value: Integer): Price =
    Price(BigDecimal(value))
}
