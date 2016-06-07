package test.mock.ws

import play.api.libs.ws._

trait MockWSClient extends WSClient {

    def webDelay: Long

    def mockResponses: Map[String, MockResponse]

    def MockNotFound = new MockWSResponse("{}", 404, "Not Found")

    def close(): Unit = ()

    def underlying[T]: T = ???

    def url(url: String): WSRequest = new MockWSRequest(url, "GET", webDelay, mockResponses(url))
}