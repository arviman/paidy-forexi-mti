package forex.services.rates.interpreters.clients

import cats.effect.Async
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import forex.config.RateApiConfig
import forex.domain.oneFrameAPI.OneFrameApiResponseRow
import org.http4s.Method.GET
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.jdkhttpclient.JdkHttpClient
import org.http4s.{Headers, HttpVersion, Request, Uri}
import wvlet.log.LogSupport

import scala.util.{Failure, Success, Try}

class RateClientProxyJavaNetImpl[A[_]: Async](config: RateApiConfig)
    extends RateClientyProxyBase[A](config)
    with LogSupport {

  def fetchResponseJdk: A[OneFrameApiResponse] =
    Try(
      Uri
        .fromString(ratesUrl.toString)
        .map(
          uri =>
            JdkHttpClient.simple[A].use { // Create a client
              httpClient: Client[A] =>
                httpClient.expectOr[OneFrameApiResponse](
                  Request[A](GET, uri, HttpVersion.`HTTP/1.1`, Headers("token" -> config.token))
                )(
                  err => {
                    error(s"Error while getting response from rate API: ${err.status}")
                    (new Throwable).pure[A]
                  }
                )
          }
        )
    ) match {
      case Success(value) =>
        value match {
          case Left(_) =>
            error("Error creating Uri")
            List[OneFrameApiResponseRow]().pure[A]
          case Right(res) =>
            res
        }
      case Failure(err) =>
        error(err.getMessage)
        List[OneFrameApiResponseRow]().pure[A]
    }

  /** Decode the response
    * @return an exception
    */
  def fetchResponse: A[OneFrameApiResponse] = {
    info("fetching response from " + ratesUrl.toString)

    Try(
      Uri
        .fromString(ratesUrl.toString)
        .map(
          uri =>
            JavaNetClientBuilder[A].resource.use { // Create a client
              httpClient: Client[A] =>
                httpClient.expectOr[OneFrameApiResponse](
                  Request[A](GET, uri, HttpVersion.`HTTP/1.1`, Headers("token" -> config.token))
                )(
                  err => {
                    error(s"Error while getting response from rate API: ${err.status}")
                    (new Throwable).pure[A]
                  }
                )
          }
        )
    ) match {
      case Success(value) =>
        value match {
          case Left(_) =>
            error("Error creating Uri")
            List[OneFrameApiResponseRow]().pure[A]
          case Right(res) =>
            res.map(x => info(s"Returning response from API ${x.size}"))
            res
        }
      case Failure(err) =>
        error(err.getMessage)
        List[OneFrameApiResponseRow]().pure[A]
    }
  }

}
