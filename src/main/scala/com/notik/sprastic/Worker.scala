package com.notik.sprastic

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }
import com.notik.sprastic.api.{ Index ⇒ ESIndex }
import com.notik.sprastic.api.{ Update ⇒ ESUpdate }
import com.notik.sprastic.api.{ Get ⇒ ESGet }
import com.notik.sprastic.api.{ Delete ⇒ ESDelete }
import com.notik.sprastic.api.{ MultiGet ⇒ ESMultiGet }
import com.notik.sprastic.api.{ Bulk ⇒ ESBulk }
import spray.http.{ HttpRequest, HttpResponse }
import com.notik.sprastic.ESActor.Response
import scala.concurrent.Future
import spray.client.pipelining._

class Worker(pipeline: Future[SendReceive], target: ActorRef) extends Actor with ActorLogging {

  import context._

  def receive = {
    case i @ ESIndex(index, t, data, id, opType) ⇒
      sendRequest(i.httpRequest)
      become(responseReceive)
    case up @ ESUpdate(index, t, data, id, version) ⇒
      sendRequest(up.httpRequest)
      become(responseReceive)
    case get @ ESGet(index, t, id) ⇒
      sendRequest(get.httpRequest)
      become(responseReceive)
    case del @ ESDelete(index, t, id) ⇒
      sendRequest(del.httpRequest)
      become(responseReceive)
    case muget @ ESMultiGet(docs) ⇒
      sendRequest(muget.httpRequest)
      become(responseReceive)
    case bulk @ ESBulk(ops) ⇒
      sendRequest(bulk.httpRequest)
      become(responseReceive)
  }
  
  import scala.util.{ Success, Failure }
  def sendRequest(req: HttpRequest) =
    pipeline.flatMap(_(req)) onComplete {
      case Success(res) ⇒ self ! res
      case Failure(ex) ⇒ log.error(s"Failure with $ex.getMessage")
    }

  def responseReceive: Receive = {
    case response: HttpResponse ⇒
      parent ! Response(response, target)
  }
}

object Worker {
  def props(pipeline: Future[SendReceive], target: ActorRef) =
    Props(new Worker(pipeline, target))
}