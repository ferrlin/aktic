package in.ferrl.aktic.core

import aktic._, Message._
import akka.actor.{ Actor, Props, ActorRef, ActorLogging }
import in.ferrl.aktic.core.{ Index ⇒ ESIndex }
import in.ferrl.aktic.core.{ Update ⇒ ESUpdate }
import in.ferrl.aktic.core.{ Get ⇒ ESGet }
import in.ferrl.aktic.core.{ Delete ⇒ ESDelete }
import in.ferrl.aktic.core.{ MultiGet ⇒ ESMultiGet }
import in.ferrl.aktic.core.{ Bulk ⇒ ESBulk }
import in.ferrl.aktic.core.{ Search ⇒ ESSearch }
import akka.http.model.{ HttpRequest, HttpResponse }
import scala.concurrent.Future
import scala.util.{ Either, Left, Right }
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.unmarshalling.Unmarshal
import akka.http.model.StatusCodes._
import java.io.IOException

class Worker(pipeline: HttpRequest ⇒ Future[HttpResponse], target: ActorRef) extends Actor
  with ActorLogging {
  import context._

  def receive: Receive = prepare andThen send

  private def prepare: PartialFunction[Any, HttpRequest] = {
    case i @ ESIndex(_, _, _, _, _) ⇒ i.httpRequest
    case up @ ESUpdate(_, _, _, _, _) ⇒ up.httpRequest
    case get @ ESGet(_, _, _) ⇒ get.httpRequest
    case del @ ESDelete(_, _, _) ⇒ del.httpRequest
    case muget @ ESMultiGet(_) ⇒ muget.httpRequest
    case bulk @ ESBulk(_) ⇒ bulk.httpRequest
    case search @ ESSearch(_, _) ⇒ search.httpRequest
  }

  private def send(req: HttpRequest): Unit = {
    send2ES(req) flatMap {
      case Right(data) ⇒ Future { WithData(data, target) }
      case Left(error) ⇒ Future { WithError(error, target) }
    } map { parent ! _ }
  }

  import akka.stream.FlowMaterializer
  implicit val mat = FlowMaterializer()(context)

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