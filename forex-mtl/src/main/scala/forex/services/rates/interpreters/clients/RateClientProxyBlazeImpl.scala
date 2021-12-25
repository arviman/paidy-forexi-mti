package forex.services.rates.interpreters.clients

import cats.effect.Async
import cats.implicits.toFunctorOps
import forex.config.RateApiConfig
import forex.http.rates.Protocol.OneFrameApiResponse
import org.http4s.Method.GET
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.{Headers, HttpVersion, Request, Uri}
import wvlet.log.LogSupport

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class RateClientProxyBlazeImpl[A[_]: Async](config: RateApiConfig)
    extends RateClientyProxyBase[A](config)
    with LogSupport {

  import forex.http.rates.Protocol.oneFrameResponseEntityDecoder
  val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  override def fetchResponse: A[OneFrameApiResponse] = {
    val uri = Uri.unsafeFromString(ratesUrl.toString)
    for {
      res <- BlazeClientBuilder[A]
              .withExecutionContext(ec)
              .resource
              .use(
                client =>
                  client.expect[OneFrameApiResponse](
                    Request[A](GET, uri, HttpVersion.`HTTP/1.1`, Headers("token" -> config.token))
                )
              )
    } yield res
  }
}
