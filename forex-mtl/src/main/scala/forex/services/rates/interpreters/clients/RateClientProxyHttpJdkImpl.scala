package forex.services.rates.interpreters.clients

import cats.effect._
import cats.implicits.catsSyntaxApplicativeId
import forex.config.RateApiConfig
import forex.http.rates.Protocol.OneFrameApiResponse
import org.http4s._
import org.http4s.client.Client
import org.http4s.jdkhttpclient.JdkHttpClient
import wvlet.log.LogSupport

class RateClientProxyHttpJdkImpl[F[_]: Async](config: RateApiConfig)
    extends RateClientyProxyBase[F](config)
    with LogSupport {
  val client: Resource[F, Client[F]] = JdkHttpClient.simple[F]
  def fetchStatus(c: Client[F], uri: Uri): F[Status] =
    c.status(Request[F](Method.GET, uri = uri))

  import forex.http.rates.Protocol.oneFrameResponseEntityDecoder

  override def fetchResponse: F[OneFrameApiResponse] = {
    debug("calling with Http JDK client")
    client.use(
      c =>
        c.expectOr[OneFrameApiResponse](
          Request[F](
            Method.GET,
            Uri.unsafeFromString(ratesUrl.toString),
            HttpVersion.`HTTP/1.1`,
            Headers("token" -> config.token)
          )
        )(
          err => {
            error(s"Error while getting response from rate API: ${err.status}")
            (new Throwable).pure[F]
          }
      )
    )

  }
}
