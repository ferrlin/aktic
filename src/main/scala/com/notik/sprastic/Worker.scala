package com.notik.sprastic

import akka.actor.{ Actor, Props, ActorRef }
import spray.httpx.RequestBuilding._
import com.notik.sprastic.api.MultiGet
import com.notik.sprastic.api.{ Get ⇒ ESGet }
import com.notik.sprastic.api.Index
import com.notik.sprastic.api.Update
import com.notik.sprastic.api.{ Delete ⇒ ESDelete }
import com.notik.sprastic.api.Docs
import com.notik.sprastic.api.Bulk
import spray.http.{ HttpRequest, HttpResponse }
import scala.concurrent.Future
import com.notik.sprastic.ElasticSearchActor.Response

class Worker2(pipeline: HttpRequest ⇒ Future[HttpResponse], target: ActorRef) extends Actor {

  import context._

  def receive = {
    case Index(index, t, data, id, opType) ⇒
      val uri = id match {
        case Some(x) ⇒ s"/$x${opType.map(op ⇒ s"?op_type=${op.value}").getOrElse("")}"
        case None ⇒ s"${opType.map(op ⇒ s"?op_type=${op.value}").getOrElse("")}"
      }
      pipeline(Post(s"/$index/$t", data))
      become(responseReceive)
    case Update(index, t, data, id, version) ⇒
      pipeline(Put(s"/$index/$t/$id${version.fold("")(v ⇒ s"?version=$v")}", data))
      become(responseReceive)
    case ESGet(index, t, id) ⇒
      pipeline(Get(s"/$index/$t/$id"))
      become(responseReceive)
    case ESDelete(index, t, id) ⇒
      pipeline(Delete(s"/$index/$t/$id"))
      become(responseReceive)
    case MultiGet(docs) ⇒
    // pipeline(Get(s"", data))
    case Bulk(ops) ⇒
      val json = ops.map(_.bulkJson).mkString("\n")
      pipeline(Post("/_bulk", json))
      become(responseReceive)
  }

  def responseReceive: Receive = {
    case response: HttpResponse ⇒
      parent ! Response(response, target)
  }
}

object Worker2 {
  def props(pipeline: HttpRequest ⇒ Future[HttpResponse], target: ActorRef) =
    Props(new Worker2(pipeline, target))
}