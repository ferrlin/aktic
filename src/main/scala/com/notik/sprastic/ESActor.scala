package com.notik.sprastic

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util._
import com.typesafe.config.{ Config }
import akka.actor.{ Actor, Props, ActorRef }
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import akka.http.Http
import akka.http.model.{ HttpRequest, HttpResponse }
// import spray.client.pipelining._
// import spray.http._

import com.notik.sprastic.client.SprasticClient
import com.notik.sprastic.config.SprasticConfig

class ESActor(config: Config) extends Actor {

  import ESActor._
  import context.dispatcher

  import akka.stream.scaladsl.{ Sink, Source }
  import akka.stream.FlowMaterializer

  implicit val system = context.system
  implicit val timeout = 1000.millis
  implicit val materializer = FlowMaterializer()

  // this is spray version.
  /*
  val pipeline: Future[SendReceive] =
    for (
      Http.HostConnectorInfo(connector, _) ← IO(Http) ? Http.HostConnectorSetup("localhost", port = 9200)
    ) yield sendReceive(connector)
*/

  def pipeline(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(esConn.flow).runWith(Sink.head)

  val esConn = Http().outgoingConnection("localhost", 9200)

  def receive = {
    case Response(httpResponse, target) ⇒
      target ! httpResponse
    case msg ⇒
      context.actorOf(Worker.props(pipeline, sender)) ! msg
  }
}

object ESActor {
  def props(config: Config = SprasticConfig.defaultConfig): Props =
    Props(new ESActor(config))
  case class Response(httpResponse: HttpResponse, target: ActorRef)
}