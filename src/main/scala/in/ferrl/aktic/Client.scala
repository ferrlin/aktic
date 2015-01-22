package in.ferrl.aktic

import akka.actor.{ ActorSystem, ActorRefFactory, ActorRef }
import com.typesafe.config.{ ConfigFactory, Config }
import scala.concurrent.Future
import akka.http.model.HttpResponse
import akka.util.Timeout
import aktic._
import in.ferrl.aktic.config.AkticConfig
import in.ferrl.aktic.api._
import scala.concurrent.duration._

trait ApiService {
  implicit val timeout: FiniteDuration = 10.seconds
  import scala.concurrent.ExecutionContext.Implicits.global

  def get(index: String, typ: String, id: String): Future[ResponseDataAsString] =
    execute(Get(index, typ, id))

  def delete(index: String, typ: String, id: String): Future[ResponseDataAsString] =
    execute(Delete(index, typ, id))

  def index(index: String, typ: String, document: String, id: Option[String]): Future[ResponseDataAsString] =
    execute(Index(index, typ, document, id))

  def update(index: String, typ: String, document: String, id: String): Future[ResponseDataAsString] =
    execute(Update(index, typ, document, id))

  def getAll(index: String): Future[ResponseDataAsString] =
    search(index, List.empty)

  def search(index: String, params: Seq[String]): Future[ResponseDataAsString] =
    execute(Search(index, params))

  protected[aktic] def execute(operation: ESOperation)(implicit timeout: FiniteDuration): Future[ResponseDataAsString]
}

class Client(config: Config = AkticConfig.defaultConfig) extends ApiService {
  import akka.pattern.ask

  val system: ActorSystem = ActorSystem("aktic-system")

  protected[aktic] def execute(operation: ESOperation)(implicit timeout: FiniteDuration): Future[ResponseDataAsString] =
    system.actorOf(ESActor.props(config)).ask(operation)(Timeout(timeout)).mapTo[ResponseDataAsString]

  def shutdown() = system.shutdown()
}

object Client {
  def apply(actorRefFactory: ActorRefFactory): ActorRef = actorRefFactory.actorOf(ESActor.props())
  def apply(actorRefFactory: ActorRefFactory, config: Config): ActorRef = actorRefFactory.actorOf(ESActor.props(config))
  def apply(config: Config): Client = new Client(config)
  def apply(): Client = new Client
}
