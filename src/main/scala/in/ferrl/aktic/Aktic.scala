package in.ferrl.aktic

import akka.actor.{ ActorSystem, ActorRefFactory, ActorRef }
import com.typesafe.config.{ ConfigFactory, Config }
import scala.concurrent.Future
import akka.http.model.HttpResponse
import akka.util.Timeout
import aktic.{ ResponseDataAsString, ErrorMessage }
import in.ferrl.aktic.config.AkticConfig
import in.ferrl.aktic.core._
import scala.concurrent.duration._
import akka.pattern.ask

trait ApiService {
  implicit val timeout: FiniteDuration = 10.seconds
  import scala.concurrent.ExecutionContext.Implicits.global

  def get(id: String)(implicit docPath: DocPath): Future[ResponseDataAsString] =
    execute(Get(docPath.index, docPath.typ, id))

  def get(index: String, typ: String, id: String): Future[ResponseDataAsString] =
    execute(Get(index, typ, id))

  def delete(id: String)(implicit docPath: DocPath): Future[ResponseDataAsString] =
    execute(Delete(docPath.index, docPath.typ, id))

  def delete(index: String, typ: String, id: String): Future[ResponseDataAsString] =
    execute(Delete(index, typ, id))

  def index(id: String, document: String)(implicit docPath: DocPath): Future[ResponseDataAsString] =
    execute(Index(docPath.index, docPath.typ, document, Some(id)))

  def index(index: String, typ: String, document: String, id: Option[String]): Future[ResponseDataAsString] =
    execute(Index(index, typ, document, id))

  def update(id: String, document: String)(implicit docPath: DocPath): Future[ResponseDataAsString] =
    execute(Update(docPath.index, docPath.typ, document, id))

  def update(index: String, typ: String, document: String, id: String): Future[ResponseDataAsString] =
    execute(Update(index, typ, document, id))

  def search(index: String, params: Seq[String]): Future[ResponseDataAsString] =
    execute(Search(index, params))

  protected[aktic] def execute(operation: Operations)(implicit timeout: FiniteDuration): Future[ResponseDataAsString]
}

class Aktic(system: ActorSystem = ActorSystem("aktic-system"), config: Config = AkticConfig.defaultConfig) extends ApiService {

  protected[aktic] def execute(operation: Operations)(implicit timeout: FiniteDuration): Future[ResponseDataAsString] =
    system.actorOf(Dispatcher.props(config)).ask(operation)(Timeout(timeout)).mapTo[ResponseDataAsString]

  def shutdown() = system.shutdown()
}

object Aktic {
  def apply(system: ActorSystem, config: Config) = new Aktic(system, config)
  def apply(system: ActorSystem) = new Aktic(system = system)
  def apply(config: Config): Aktic = new Aktic(config = config)
  def apply(): Aktic = new Aktic
}
