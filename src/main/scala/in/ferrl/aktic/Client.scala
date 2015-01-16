package in.ferrl.aktic

import akka.actor.{ ActorSystem, ActorRefFactory, ActorRef }
import com.typesafe.config.{ ConfigFactory, Config }
import scala.concurrent.Future
import akka.http.model.HttpResponse
import akka.util.Timeout
import in.ferrl.aktic.config.AkticConfig
import in.ferrl.aktic.api._
import in.ferrl.aktic._
import scala.concurrent.duration._

trait ApiService {
  implicit val timeout: FiniteDuration = 10 seconds
  import scala.concurrent.ExecutionContext.Implicits.global

  def get: (String, String, String) ⇒ Future[HttpResponse] = (index, typ, id) ⇒ execute(Get(index, typ, id))

  def delete: (String, String, String) ⇒ Future[HttpResponse] = (index, typ, id) ⇒ execute(Delete(index, typ, id))

  def index(index: String, typ: String, document: String, id: Option[String] = None) =
    execute(Index(index, typ, document, id))

  def update(index: String, typ: String, document: String, id: String) =
    execute(Update(index, typ, document, id))

  def execute(operation: ESOperation)(implicit timeout: FiniteDuration): Future[HttpResponse]
}

class Client(config: Config = AkticConfig.defaultConfig) extends ApiService {
  import akka.pattern.ask
  val system: ActorSystem = ActorSystem("aktic-system")
  def execute(operation: ESOperation)(implicit timeout: FiniteDuration): Future[HttpResponse] =
    system.actorOf(ESActor.props(config)).ask(operation)(Timeout(timeout)).mapTo[HttpResponse]
  def shutdown() = system.shutdown()
}

object Client {
  def apply(actorRefFactory: ActorRefFactory): ActorRef =
    actorRefFactory.actorOf(ESActor.props())
  def apply(actorRefFactory: ActorRefFactory, config: Config): ActorRef =
    actorRefFactory.actorOf(ESActor.props(config))
  def apply(config: Config): Client = new Client(config)
  def apply(): Client = new Client
}
