package forex.http.rates

import forex.domain.Currency.show
import forex.domain.Rate.Pair
import forex.domain._
import io.circe._
import io.circe.generic.extras.Configuration
import forex.domain.oneFrameAPI.OneFrameApiResponseRow
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}

object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  final case class GetApiRequest(
      from: Currency,
      to: Currency
  )

  final case class GetApiResponse(
      from: Currency,
      to: Currency,
      price: Price,
      timestamp: Timestamp
  )

  implicit val currencyEncoder: Encoder[Currency] =
    Encoder.instance[Currency] { show.show _ andThen Json.fromString }

  implicit val currencyDecoder: Decoder[Currency] = deriveConfiguredDecoder[Currency]

  //deriveConfiguredEncoder[Price]
  implicit val priceEncoder: Encoder[Price] = Encoder.encodeString.contramap[Price](x=>x.value.setScale(8, BigDecimal.RoundingMode.UP).toString())

  implicit val pairEncoder: Encoder[Pair] =
    deriveConfiguredEncoder[Pair]

  implicit val rateEncoder: Encoder[Rate] =
    deriveConfiguredEncoder[Rate]

  implicit val responseEncoder: Encoder[GetApiResponse] =
    deriveConfiguredEncoder[GetApiResponse]

  implicit val encodeTimestamp: Encoder.AsObject[Timestamp] = deriveConfiguredEncoder[Timestamp]


  implicit val oneFrameApiResponseRowDecoder: Decoder[OneFrameApiResponseRow] = deriveConfiguredDecoder[OneFrameApiResponseRow]

  type OneFrameApiResponse = List[OneFrameApiResponseRow]
  implicit val OneFrameResponseDecoder = Decoder[OneFrameApiResponse].prepare(_.root)

  implicit val oneFrameApiResponseRowEncode: Encoder.AsObject[OneFrameApiResponseRow] = deriveConfiguredEncoder[OneFrameApiResponseRow]

}
