package forex.services.rates.interpreters

import cats.effect.IO
import forex.domain.Rate
import forex.services.rates.RateClientProxy

/*import org.http4s.Method.GET
import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.client.blaze._*/
class RateClientProxyImpl extends RateClientProxy {
  override def getRates(): IO[List[Rate]] = ???

  /*
  private def fetchFromService() = {
    import org.http4s.Uri.uri
    import cats.effect.IO
    import io.circe.generic.auto._
    import fs2.Stream

    // Decode the response
    def fetch(name: String): Stream[IO, Hello] = {
      // todo change from config
      val req = GET(uri("http://localhost:8080/")) // add in list of query pairs
      // Create a client
      BlazeClientBuilder[IO](ec).stream.flatMap { httpClient =>
        Stream.eval(httpClient.expect(req)(jsonOf[IO, OneFrameAPIResponse]))
      }
    }
  }*/
}


