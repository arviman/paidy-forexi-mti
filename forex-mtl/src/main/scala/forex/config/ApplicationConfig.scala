package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    rateApi: RateApiConfig,
    pollDuration: FiniteDuration
)

case class RateApiConfig(baseUri: String, port: Int, token: String)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)
