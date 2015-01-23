package in.ferrl.aktic.core

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
import aktic._, Message._
import in.ferrl.aktic.config.AkticConfig

class Dispatcher(config: Config) extends Actor {

  import context.dispatcher
  import akka.stream.scaladsl.{ Sink, Source }
  import akka.stream.FlowMaterializer

  implicit val system = context.system
  implicit val timeout = 1000.millis
  implicit val materializer = FlowMaterializer()

  def pipeline(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(esConn.flow).runWith(Sink.head)

  val esConn = Http().outgoingConnection(config.getString("host"), config.getInt("port"))

  def receive = {
    // case Response(httpResponse, target) ⇒
    // target ! httpResponse
    case WithData(data, target) ⇒
      target ! data
    case WithError(err, target) ⇒
      target ! err
    case msg ⇒
      context.actorOf(Worker.props(pipeline, sender)) ! msg
  }
}

object Dispatcher {
  def props(config: Config = AkticConfig.defaultConfig): Props = Props(new Dispatcher(config))
  // case class Response(httpResponse: HttpResponse, target: ActorRef)
}