import dispatch._
import dispatch.Defaults._
import java.text.SimpleDateFormat
import java.util.Locale
import scala.util.{Try, Success, Failure}
import scala.collection.JavaConverters._

object DispatchDemo extends App {
  // val urlStr = "http://dispatch.databinder.net"
  val urlStr = "http://localhost:8000/"

  // val res = Http(url(urlStr).secure > as.Response(x => x.getHeaders)).option
  // println("----------------------------------------------------------------------")
  // println(res())

  // val res = Http.configure(_.setFollowRedirects(true))(url(urlStr).HEAD OK as.String)
  // println("----------------------------------------------------------------------")
  // println(res())

  val client = Http.configure { builder =>
    builder
      .setFollowRedirects(true)
      .setRequestTimeoutInMs(10000)
      .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36")
  }

  val asHeaders = as.Response { res =>
    mapAsScalaMapConverter(res.getHeaders).asScala.mapValues(_.asScala.toList)
  }

  def getLastModified(urlStr: String): Try[String] = Try {
    val headers = client(url(urlStr).HEAD OK asHeaders)

    headers.onComplete {
      case Success(r) => println("Success: " + r)
      case Failure(_) => println("Failure")
    }

    headers().get("Last-Modified").get.head
  }

  def getLastModifiedSeconds(urlStr: String): Try[Int] = Try {
    val format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
    format.setLenient(false)
    (format.parse(getLastModified(urlStr).get).getTime / 1000).toInt
  }

  val lastModSec = getLastModifiedSeconds(urlStr).recover{
    case _: Throwable => -1
  }.get

  println(lastModSec)

  client.shutdown
}
