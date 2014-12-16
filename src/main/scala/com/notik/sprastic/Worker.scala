package com.notik.sprastic

import akka.actor.{ Actor, Props, ActorRef }
import com.notik.sprastic.api.ESMultiGet
import com.notik.sprastic.api.ESGet
import com.notik.sprastic.api.ESIndex
import com.notik.sprastic.api.ESUpdate
import com.notik.sprastic.api.ESDelete
import com.notik.sprastic.api.Docs
import com.notik.sprastic.api.ESBulk
import spray.http.{ HttpRequest, HttpResponse }
import com.notik.sprastic.ESActor.Response
import scala.concurrent.Future
import spray.client.pipelining._

class Worker2(pipeline: Future[SendReceive], target: ActorRef) extends Actor {

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
    case bulk @ ESBulk(ops) ⇒
      sendRequest(bulk.httpRequest)
      become(responseReceive)
  }

  def sendRequest(req: Option[HttpRequest]) =
    pipeline flatMap (_(req.get))

  def responseReceive: Receive = {
    case response: HttpResponse ⇒
      parent ! Response(response, target)
  }
}

object Worker2 {
  def props(pipeline: Future[SendReceive], target: ActorRef) =
    Props(new Worker2(pipeline, target))
}