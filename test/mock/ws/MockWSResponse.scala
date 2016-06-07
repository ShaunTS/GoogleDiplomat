package test.mock.ws

import akka.util.ByteString
import org.asynchttpclient.{Response => AHCResponse}
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws._


class MockWSResponse(val body: String, val status: Int, val statusText: String) extends MockResponse

trait MockResponse extends WSResponse {

    val ahcResponse: AHCResponse = null

    def body: String

    def status: Int

    def statusText: String

    def allHeaders = Map.empty[String, Seq[String]]

    def bodyBytes: Array[Byte] = body.getBytes("utf-8")

    def bodyAsBytes: ByteString = ByteString(bodyBytes)

    def json: JsValue = Json.parse(bodyBytes)


    def header(key: String): Option[String] = allHeaders.get(key).flatMap(_.headOption)

    def cookie(name: String): Option[WSCookie] = None

    def cookies: Seq[WSCookie] = Nil

    /* Supposed to use Play.XML but its not accessible */
    def xml: scala.xml.Elem = scala.xml.XML.loadString(body)

    def underlying[T]: T = ???
}