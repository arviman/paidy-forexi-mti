package forex.domain

import java.time.OffsetDateTime

case class Timestamp(value: OffsetDateTime) extends AnyVal

object Timestamp {
  def now: Timestamp =
    Timestamp(OffsetDateTime.now)

  def apply(str: String): Timestamp = Timestamp(OffsetDateTime.parse(str))
}
