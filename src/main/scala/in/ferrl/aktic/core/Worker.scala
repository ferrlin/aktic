package in.ferrl.aktic.core

import aktic._, Message._
import akka.actor.{ Actor, Props, ActorRef, ActorLogging }
import in.ferrl.aktic.core.{
    Index ⇒ ESIndex,
    Update ⇒ ESUpdate,
    Get ⇒ ESGet,
    Delete ⇒ ESDelete,
    MultiGet ⇒ ESMultiGet,
    Bulk ⇒ ESBulk,
    Search ⇒ ESSearch
}
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.model.StatusCodes._
import akka.pattern.AskTimeoutException
import scala.concurrent.Future
import scala.util.{ Either, Left, Right }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.unmarshalling.Unmarshal
import java.io.IOException

class Worker(pipeline: HttpRequest ⇒ Future[HttpResponse], target: ActorRef) extends Actor
    with ActorLogging {
    import context.dispatcher
    import context.parent
    import akka.stream.ActorMaterializer
    implicit val mat = ActorMaterializer()

    def receive: Receive = prepare andThen send

    private def prepare: PartialFunction[Any, HttpRequest] = {
        case i: ESIndex ⇒ i.httpRequest
        case up: ESUpdate ⇒ up.httpRequest
        case get: ESGet ⇒ get.httpRequest
        case del: ESDelete ⇒ del.httpRequest
        case muget: ESMultiGet ⇒ muget.httpRequest
        case bulk: ESBulk ⇒ bulk.httpRequest
        case search: ESSearch ⇒ search.httpRequest
    }

    private def send(req: HttpRequest): Unit = {
        send2ES(req) flatMap {
            case Right(data) ⇒ Future { WithData(data, target) }
            case Left(error) ⇒ Future { WithError(error, target) }
        } map { parent ! _ }
    }

    private def send2ES(req: HttpRequest): Future[Either[ErrorMessage, ResponseDataAsString]] =
        pipeline(req).flatMap { response ⇒
            response.status match {
                case OK ⇒ Unmarshal(response.entity).to[ResponseDataAsString].map(Right(_))
                case Created ⇒ Unmarshal(response.entity).to[ResponseDataAsString].map(Right(_))
                case BadRequest ⇒ Future.successful(Left("Bad request.. Please check"))
                case _ ⇒ Unmarshal(response.entity).to[ErrorMessage].flatMap { entity ⇒
                    val errorMsg = s"Request failed with status code ${response.status} and entity $entity"
                    log.error(errorMsg)
                    Future.failed(new IOException(errorMsg))
                }
            }
        }
}

object Worker {
    def props(pipeline: HttpRequest ⇒ Future[HttpResponse], target: ActorRef) = Props(new Worker(pipeline, target))
}