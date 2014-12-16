package com.notik.sprastic

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util._
import com.typesafe.config.{ Config }
import akka.actor.{ Actor, Props, ActorRef }
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import spray.client.pipelining._
import spray.http._

import com.notik.sprastic.client.SprasticClient
import com.notik.sprastic.config.SprasticConfig

class ESActor(config: Config) extends Actor {

  import ESActor._
  import context.dispatcher

  implicit val system = context.system
  implicit val timeout: Timeout = 10 minutes

  val pipeline: Future[SendReceive] =
    for (
      Http.HostConnectorInfo(connector, _) ← IO(Http) ? Http.HostConnectorSetup("localhost", port = 9200)
    ) yield sendReceive(connector)

  def receive = {
    case Response(httpsResponse, target) ⇒
      target ! httpsResponse
    case msg ⇒
      context.actorOf(Worker2.props(pipeline, sender)) ! msg
  }
}

object ESActor {
  def props(config: Config = SprasticConfig.defaultConfig): Props =
    Props(new ESActor(config))
  case class Response(httpResponse: HttpResponse, target: ActorRef)
}