package com.notik.sprastic

import akka.actor.{ Actor, Props, ActorRef }
import com.notik.sprastic.api.MultiGet
import com.notik.sprastic.api.{ Get ⇒ ESGet }
import com.notik.sprastic.api.Index
import com.notik.sprastic.api.Update
import com.notik.sprastic.api.{ Delete ⇒ ESDelete }
import com.notik.sprastic.api.Docs
import com.notik.sprastic.api.Bulk
import spray.http.{ HttpRequest, HttpResponse }
import com.notik.sprastic.ESActor.Response
import scala.concurrent.Future
import spray.client.pipelining._

class Worker2(pipeline: Future[SendReceive], target: ActorRef) extends Actor {

  import context._

  def receive = {
    case Index(index, t, data, id, opType) ⇒
      val uri = id match {
        case Some(x) ⇒ s"/$x${opType.map(op ⇒ s"?op_type=${op.value}").getOrElse("")}"
        case None ⇒ s"${opType.map(op ⇒ s"?op_type=${op.value}").getOrElse("")}"
      }
      sendRequest(Post(s"/$index/$t", data))
      become(responseReceive)
    case Update(index, t, data, id, version) ⇒
      sendRequest(Put(s"/$index/$t/$id${version.fold("")(v ⇒ s"?version=$v")}", data))
      become(responseReceive)
    case ESGet(index, t, id) ⇒
      sendRequest(Get(s"/$index/$t/$id"))
      become(responseReceive)
    case ESDelete(index, t, id) ⇒
      sendRequest(Delete(s"/$index/$t/$id"))
      become(responseReceive)
    case MultiGet(docs) ⇒
    // pipeline(Get(s"", data))
    case Bulk(ops) ⇒
      val json = ops.map(_.bulkJson).mkString("\n")
      sendRequest(Post("/_bulk", json))
      become(responseReceive)
  }

  def sendRequest(req: HttpRequest) =
    pipeline flatMap (_(req))

  def responseReceive: Receive = {
    case response: HttpResponse ⇒
      parent ! Response(response, target)
  }
}

object Worker2 {
  def props(pipeline: Future[SendReceive], target: ActorRef) =
    Props(new Worker2(pipeline, target))
}