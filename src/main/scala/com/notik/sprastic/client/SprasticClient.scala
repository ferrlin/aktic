package com.notik.sprastic.client

import akka.actor.{ ActorSystem, ActorRefFactory, ActorRef }
import com.notik.sprastic.ESActor
import com.typesafe.config.{ ConfigFactory, Config }
import com.notik.sprastic.api._
import scala.concurrent.Future
import spray.http.HttpResponse
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration
import com.notik.sprastic.config.SprasticConfig

trait ApiService {
  import scala.concurrent.duration._
  implicit val timeout: FiniteDuration = 10 seconds

  def get: (String, String, String) ⇒ Future[HttpResponse] = (index, typ, id) ⇒ execute(Get(index, typ, id))

  def delete: (String, String, String) ⇒ Future[HttpResponse] = (index, typ, id) ⇒ execute(Delete(index, typ, id))

  def index(index: String, typ: String, document: String, id: Option[String] = None) =
    execute(Index(index, typ, document, id))

  def update(index: String, typ: String, document: String, id: String) =
    execute(Update(index, typ, document, id))

  def execute(operation: ESOperation)(implicit timeout: FiniteDuration): Future[HttpResponse]
}

class SprasticClient(config: Config = SprasticConfig.defaultConfig) extends ApiService {
  import akka.pattern.ask
  val system: ActorSystem = ActorSystem("sprastic-actor-system")
  def execute(operation: ESOperation)(implicit timeout: FiniteDuration): Future[HttpResponse] =
    system.actorOf(ESActor.props(config)).ask(operation)(Timeout(timeout)).mapTo[HttpResponse]
  def shutdown() = system.shutdown()
}

object SprasticClient {

  def apply(actorRefFactory: ActorRefFactory): ActorRef =
    actorRefFactory.actorOf(ESActor.props())

  def apply(actorRefFactory: ActorRefFactory, config: Config): ActorRef =
    actorRefFactory.actorOf(ESActor.props(config))

  def apply(config: Config): SprasticClient = new SprasticClient(config)

  def apply(): SprasticClient = new SprasticClient
}
