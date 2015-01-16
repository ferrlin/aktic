package in.ferrl.aktic

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }
import in.ferrl.aktic.api.{ Index ⇒ ESIndex }
import in.ferrl.aktic.api.{ Update ⇒ ESUpdate }
import in.ferrl.aktic.api.{ Get ⇒ ESGet }
import in.ferrl.aktic.api.{ Delete ⇒ ESDelete }
import in.ferrl.aktic.api.{ MultiGet ⇒ ESMultiGet }
import in.ferrl.aktic.api.{ Bulk ⇒ ESBulk }
import akka.http.model.{ HttpRequest, HttpResponse }
import scala.concurrent.Future
import scala.util.{ Either, Left, Right }
import akka.http.marshallers.sprayjson.SprayJsonSupport._
// import akka.http.marshalling.ToResponseMarshallable
import akka.http.unmarshalling.Unmarshal
import akka.http.model.StatusCodes._
import java.io.IOException
import in.ferrl.aktic.ESActor._
import spray.json.DefaultJsonProtocol

class Worker(pipeline: HttpRequest ⇒ Future[HttpResponse], target: ActorRef) extends Actor with ActorLogging with DefaultJsonProtocol {
  import context._

  def receive = {
    case i @ ESIndex(index, t, data, id, opType) ⇒
      sendRequest(i.httpRequest)
    case up @ ESUpdate(index, t, data, id, version) ⇒
      sendRequest(up.httpRequest)
    case get @ ESGet(index, t, id) ⇒
      sendRequest(get.httpRequest)
    case del @ ESDelete(index, t, id) ⇒
      sendRequest(del.httpRequest)
    case muget @ ESMultiGet(docs) ⇒
      sendRequest(muget.httpRequest)
    case bulk @ ESBulk(ops) ⇒
      sendRequest(bulk.httpRequest)
  }

  def sendRequest(req: HttpRequest) = {
    val responseFuture = send2ES(req)
    responseFuture map { resp ⇒
      resp match {
        case Right(data) ⇒ parent ! WithData(data, target)
        case Left(error) ⇒ parent ! WithError(error, target)
      }
    }
  }

  type ErrorMessage = String
  type ResponseData = String

  def send2ES(req: HttpRequest): Future[Either[ErrorMessage, ResponseData]] = {
    pipeline(req).flatMap { response ⇒
      response.status match {
        case OK ⇒ Unmarshal(response.entity).to[ResponseData].map(Right(_))
        case BadRequest ⇒ Future.successful(Left("Bad request.. Please check"))
        case _ ⇒ Unmarshal(response.entity).to[ErrorMessage].flatMap { entity ⇒
          val errorMsg = s"Request failed with status code ${response.status} and entity $entity"
          log.error(errorMsg)
          Future.failed(new IOException(errorMsg))
        }
      }
    }
  }
}

object Worker {
  def props(pipeline: HttpRequest ⇒ Future[HttpResponse], target: ActorRef) =
    Props(new Worker(pipeline, target))
}