package test.mock.ws

import play.api.libs.iteratee.Enumerator
import play.api.libs.ws._
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}


class MockWSRequest(
    val url: String,
    val method: String,
    val webDelay: Long,
    val mockResponse: MockResponse
) extends MockRequest


trait MockRequest extends WSRequest with SleepyFutures {

    val url: String

    val method: String

    val webDelay: Long

    val mockResponse: MockResponse

    override def get() = get(ec = play.api.libs.concurrent.Execution.defaultContext)

    def get(implicit ec: ExecutionContext): Future[WSResponse] = sleepily(webDelay)(mockResponse)

    val requestTimeout: Option[Int] = None

    val queryString: Map[String, Seq[String]] = Map()

    val body: WSBody = EmptyBody

    val headers: Map[String, Seq[String]] = Map()

    val auth: Option[(String, String, WSAuthScheme)] = None

    val followRedirects: Option[Boolean] = None

    val proxyServer: Option[WSProxyServer] = None

    val virtualHost: Option[String] = None


    val calc: Option[WSSignatureCalculator] = None

    def streamWithEnumerator(): Future[(WSResponseHeaders, Enumerator[Array[Byte]])] = ???

    def stream(): Future[StreamedResponse] = ???

    def execute(): Future[WSResponse] = ???

    def sign(calc: WSSignatureCalculator): WSRequest = ???

    def withAuth(username: String,password: String,scheme: WSAuthScheme): WSRequest = ???

    def withBody(body: WSBody): WSRequest = ???

    def withFollowRedirects(follow: Boolean): WSRequest = ???

    def withHeaders(hdrs: (String, String)*): WSRequest = ???

    def withMethod(method: String): WSRequest = ???

    def withProxyServer(proxyServer: WSProxyServer): WSRequest = ???

    def withQueryString(parameters: (String, String)*): WSRequest = ???

    def withRequestFilter(filter: WSRequestFilter): WSRequest = ???

    def withRequestTimeout(timeout: Duration): WSRequest = ???

    def withVirtualHost(vh: String): WSRequest = ???
}

trait SleepyFutures {

    def sleepily[T](ms: Long)(op: => T)(implicit ec: ExecutionContext) = Future[T] {
        sleep(ms)
        op
    }

    def sleep(milliseconds: Long) = (Thread sleep milliseconds)
}