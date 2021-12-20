package forex.domain
import scala.math.BigDecimal
case class Price(value: BigDecimal) extends AnyVal {
  @throws(classOf[ArithmeticException])
  def invertValue: Price = Price( BigDecimal.valueOf(1) / value)
}

object Price {
  def apply(value: Int): Price =
    Price(BigDecimal(value))
  def apply(value: Double): Price =
    Price(BigDecimal(value))
}
