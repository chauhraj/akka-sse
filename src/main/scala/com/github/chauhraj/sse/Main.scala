package com.github.chauhraj.sse

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshalling.Marshaller._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry, LoggingMagnet}
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import de.heikoseeberger.akkasse.ServerSentEvent
import spray.json.JsonWriter

import scala.reflect._

/**
  * Created by chauhraj on 11/5/16.
  */
object Main {

  def main(args: Array[String]): Unit = {
    implicit val actorSystem = ActorSystem()
    implicit val actorMaterializer = ActorMaterializer()

    val port: Int = 8000
    Http().bindAndHandle(routes, "0.0.0.0", port)
    actorSystem.log.info(s"Server running at port $port")
  }

  val start = ByteString.empty
  val sep = ByteString("\n")
  val end = ByteString.empty

  import Fill._

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json().withFramingRenderer(Flow[ByteString].intersperse(start, sep, end))

  import de.heikoseeberger.akkasse.EventStreamMarshalling._

  // logs just the request method and response status at info level
  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): RouteResult => Option[LogEntry] = {
    case RouteResult.Complete(res) => Some(LogEntry(s"Requested Method:${req.method.name}, and got response status:${res.status}, and response:${res.entity}", Logging.InfoLevel))
    case _ => None // no log entries for rejections
  }

  // This one doesn't use the implicit LoggingContext but uses `println` for logging
  def printRequestMethodAndResponseStatus(req: HttpRequest)(res: RouteResult): Unit =
    println(requestMethodAndResponseStatusAsInfo(req)(res).map(_.obj.toString).getOrElse(""))

  val logRequestResultPrintln = DebuggingDirectives.logRequestResult(LoggingMagnet(_ => printRequestMethodAndResponseStatus))

  import ch.megard.akka.http.cors.CorsDirectives._

  def routes: Route = cors() {
    pathPrefix("subscribe") {
      path("fills") {
        get {
          logRequestResultPrintln {
            complete {
              Source.actorPublisher[Fill](FillProvider[Fill])
                .map(fill ⇒ sse(fill))
            }
          }
        }
      } ~ path("employees") {
        get {
          logRequestResultPrintln {
            complete {
              Source.actorPublisher[Employee](FillProvider[Employee])
                .map(e ⇒ sse(e))
            }
          }
        }
      } ~ pathEndOrSingleSlash {
        get {
          getFromFile("./dist/index.html")
        }
      } ~ pathPrefix("assets") {
        get {
          getFromDirectory("./dist/assets")
        }
      }
    }
  }

  def sse[T: ClassTag](obj: T)(implicit jsoniser: JsonWriter[T]): ServerSentEvent = {
    val data = jsoniser.write(obj).compactPrint
    println("Sending data:" + data)
    ServerSentEvent(data = data, `type` = classTag[T].runtimeClass.getSimpleName)
  }

}
