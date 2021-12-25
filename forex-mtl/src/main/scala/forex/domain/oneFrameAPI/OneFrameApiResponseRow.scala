package forex.domain.oneFrameAPI

case class OneFrameApiResponseRow(from: String,
                                  to: String,
                                  bid: BigDecimal,
                                  ask: BigDecimal,
                                  price: BigDecimal,
                                  time_stamp: String)
