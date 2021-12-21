package forex.programs.oneFrameAPI

import forex.domain.{Currency, Timestamp}

case class OneFrameApiResponse (arr: Array[OneFrameApiResponseRow]=Array())
case class OneFrameApiResponseRow (from: Currency, to: Currency, bid: BigDecimal, ask: BigDecimal, price: BigDecimal, time_stamp: Timestamp)
