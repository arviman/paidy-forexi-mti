package forex.programs.oneFrameAPI

import forex.domain.Currency

case class OneFrameApiResponseRow (from: Currency, to: Currency, bid: BigDecimal, ask: BigDecimal, price: BigDecimal, time_stamp: String)
