package in.ferrl.aktic.core

import akka.http.scaladsl.client.RequestBuilding.{
  Put ⇒ APut,
  Post => APost,
  Get => AGet,
  Delete => ADelete
}
import akka.http.scaladsl.model.HttpRequest
import scala.concurrent.ExecutionContext

sealed trait Operations {
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
  extends Operations /*with BulkSupport*/ {

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
  extends Operations /*with BulkSupport */ {

  val httpRequest = APost(s"/$index/$typ/$id${version.fold("")(v ⇒ s"?version=$v")}", document)
}

case class Delete(index: String, typ: String, id: String) extends Operations /*with BulkSupport */ {
  val httpRequest = ADelete(s"/$index/$typ/$id")
}

case class MultiGet(docs: Seq[DocPath]) extends Operations {
  val httpRequest = AGet("/_mget")
}

case class Get(index: String, typ: String, id: String) extends Operations {
  val httpRequest = AGet(s"/$index/$typ/$id")
}

case class Search(index: String, params: Seq[String] = Seq.empty) extends Operations {
  val httpRequest = if (params.nonEmpty) AGet(s"""/$index/_search?${params.mkString("&")}""") else AGet(s"/$index/_search")
}

case class Bulk(actions: Seq[BulkSupport])(implicit ec: ExecutionContext) {
  lazy val json = actions.map(_.bulkJson).mkString("\n")
  val httpRequest = APost("/_bulk", json)
}
