package in.ferrl.aktic.api

import akka.http.client.RequestBuilding.{ Put ⇒ APut }
import akka.http.client.RequestBuilding.{ Post ⇒ APost }
import akka.http.client.RequestBuilding.{ Get ⇒ AGet }
import akka.http.client.RequestBuilding.{ Delete ⇒ ADelete }
import akka.http.model.HttpRequest
import scala.concurrent.ExecutionContext

sealed trait ESOperation {
  val httpRequest: HttpRequest
}

sealed trait BulkSupport {
  def bulkJson: String
}

sealed trait OpType {
  def value: String
}
case object Create extends OpType {
  override def value: String = "create"
}

case class Index(index: String,
  t: String,
  document: String,
  id: Option[String] = None,
  opType: Option[OpType] = None)(implicit ec: ExecutionContext)
  extends ESOperation /*with BulkSupport*/ {

  lazy val uri = id match {
    case Some(x) ⇒ s"$x${opType.map(op ⇒ s"?op_type=${op.value}").getOrElse("")}"
    case None ⇒ s"${opType.map(op ⇒ s"?op_type=${op.value}").getOrElse("")}"
  }

  val httpRequest = id match {
    case Some(_) ⇒ APut(s"/$index/$t/$uri", document)
    case _ ⇒ APost(s"/$index/$t/$uri", document)
  }
}

case class Update(index: String,
  typ: String,
  document: String,
  id: String,
  version: Option[Int] = None)(implicit ec: ExecutionContext)
  extends ESOperation /*with BulkSupport */ {

  val httpRequest = APost(s"/$index/$typ/$id${version.fold("")(v ⇒ s"?version=$v")}", document)
}

case class Delete(index: String, typ: String, id: String) extends ESOperation /*with BulkSupport */ {
  val httpRequest = ADelete(s"/$index/$typ/$id")
}

case class MultiGet(docs: Seq[Doc]) extends ESOperation {
  val httpRequest = AGet("/_mget")
}

case class Get(index: String, typ: String, id: String) extends ESOperation {
  val httpRequest = AGet(s"/$index/$typ/$id")
}

case class Search(index: String, params: Seq[String] = Seq.empty) extends ESOperation {
  val httpRequest = if (params.nonEmpty) AGet(s"""/$index/_search?${params.mkString("&")}""") else AGet(s"/$index/_search")
}

case class Bulk(actions: Seq[BulkSupport])(implicit ec: ExecutionContext) {
  lazy val json = actions.map(_.bulkJson).mkString("\n")
  val httpRequest = APost("/_bulk", json)
}
