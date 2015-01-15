package com.notik.sprastic

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }
import com.notik.sprastic.api.{ Index ⇒ ESIndex }
import com.notik.sprastic.api.{ Update ⇒ ESUpdate }
import com.notik.sprastic.api.{ Get ⇒ ESGet }
import com.notik.sprastic.api.{ Delete ⇒ ESDelete }
import com.notik.sprastic.api.{ MultiGet ⇒ ESMultiGet }
import com.notik.sprastic.api.{ Bulk ⇒ ESBulk }
// import spray.http.{ HttpRequest, HttpResponse }
import akka.http.model.{ HttpRequest, HttpResponse }
import com.notik.sprastic.ESActor.Response
import scala.concurrent.Future
import spray.client.pipelining._

class Worker(pipeline: HttpRequest ⇒ Future[HttpResponse], target: ActorRef) extends Actor with ActorLogging {

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

  import scala.util.{ Success, Failure }
  def sendRequest(req: HttpRequest) =
    pipeline(req) onComplete {
      case Success(res) ⇒ parent ! Response(res, target)
      case Failure(e) ⇒ log.error(s"Failure with $e.getMessage")
    }
}

object Worker {
  // def props(pipeline: Future[SendReceive], target: ActorRef) =
  // Props(new Worker(pipeline, target))
  def props(pipeline: HttpRequest ⇒ Future[HttpResponse], target: ActorRef) =
    Props(new Worker(pipeline, target))
}