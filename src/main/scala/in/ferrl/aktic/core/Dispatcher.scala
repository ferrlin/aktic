package in.ferrl.aktic.core

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util._
import com.typesafe.config.Config
import akka.actor.{
    Actor,
    Props,
    ActorRef,
    ActorLogging
}
import akka.io.IO
import akka.util.Timeout
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import aktic._, Message._
import in.ferrl.aktic.config.AkticConfig

class Dispatcher(config: Config) extends Actor with ActorLogging {

    import context.dispatcher
    import akka.stream.scaladsl.{ Flow, Sink, Source }
    import akka.stream.ActorMaterializer

    implicit val system = context.system
    implicit val timeout = 10.seconds
    implicit val materializer = ActorMaterializer()

    def pipeline(request: HttpRequest): Future[HttpResponse] =
        Source.single(request).via(esConn).runWith(Sink.head)

    val esConn: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] = Http().outgoingConnection(config.getString("host"), config.getInt("port"))

    def receive = {
        case WithData(data, target) ⇒
            log.info(s"Receive with data")
            target ! data
        case WithError(err, target) ⇒
            log.info(s"Receive with error")
            target ! err
        case msg ⇒
            context.actorOf(Worker.props(pipeline, sender)) ! msg
    }
}

object Dispatcher {
    def props(config: Config = AkticConfig.defaultConfig): Props = Props(new Dispatcher(config))
}