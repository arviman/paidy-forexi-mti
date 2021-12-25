package forex.services.rates.interpreters

import cats.effect.Async
import forex.config.RateApiConfig
import org.http4s.Method.GET
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.{Headers, HttpVersion, Request, Uri}
import wvlet.log.LogSupport
import cats.implicits.toFunctorOps
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}


class RateClientProxyBlazeImpl[A[_] : Async](config: RateApiConfig) extends RateClientyProxyBase[A](config) with LogSupport {

  val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  override def fetchResponse: A[OneFrameApiResponse] = {
    val uri = Uri.unsafeFromString(ratesUrl.toString)
    for {
      res <- BlazeClientBuilder[A].withExecutionContext(ec).resource.use(client =>
        client.expect[OneFrameApiResponse](Request[A](GET, uri, HttpVersion.`HTTP/1.1`, Headers("token" -> config.token)))
      )
    } yield res
  }
}


