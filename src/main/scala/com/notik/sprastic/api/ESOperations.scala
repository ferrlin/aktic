package com.notik.sprastic.api

import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

/** Elastic Search responses */
sealed trait Response {
  def index: Option[String] = None
  def `type`: Option[String] = None
  def id: Option[String] = None
  def version: Option[Int] = None
}
/* For Update Response, the created is expected to be `false`*/
case object ES_Index extends Response {
  def created: Option[Boolean] = None
}
sealed trait Found {
  def found: Option[Boolean]
}
case object ES_Retrieve extends Response with Found {
  def found: Option[Boolean] = None
  def source: Option[String] = None
}
case object ES_Delete extends Response with Found {
  def found: Option[Boolean] = None
}

case object ES_Error extends Response {
  def error: Option[String] = None
  def status: Option[Int] = None
}

sealed trait ES_API

sealed trait ESOperation

sealed trait OpType {
  def value: String
}
case object Create extends OpType {
  override def value: String = "create"
}

import spray.httpx.RequestBuilding._
import spray.http.HttpRequest

case class Index(index: String,
  t : String,
  document: String,
  id: Option[String] = None,
  opType: Option[OpType] = None)
  extends ESOperation with BulkSupport {

  override def bulkJson: String = {
    val action = opType match {
      case Some(Create) ⇒ "create"
      case _ ⇒ "index"
    }
    val actionAndMetadata = compact(render(action -> ("_index" -> index) ~ ("_type" -> t) ~ ("_id" -> id)))
    s"""
      |$actionAndMetadata
      |$document
    """.stripMargin
  }

  lazy val uri = id match {
    case Some(x) ⇒ s"$x${opType.map(op ⇒ s"?op_type=${op.value}").getOrElse("")}"
    case None ⇒ s"${opType.map(op ⇒ s"?op_type=${op.value}").getOrElse("")}"
  }

  def toHttpRequest: Option[HttpRequest] = id match {
    case Some(_) ⇒ Some(Put(s"/$index/$t/$uri", document))
    case _ ⇒ Some(Post(s"/$index/$t/$uri", document))
  }
}

case class Update(index: String,
  `type`: String,
  document: String,
  id: String,
  version: Option[Int] = None)
  extends ESOperation with BulkSupport {
  override def bulkJson: String = {
    val actionAndMetadata = compact(render("update" -> ("_index" -> index) ~ ("_type" -> `type`) ~ ("_id" -> id)))
    val doc = s""" {"doc": $document} """
    s"""
      |$actionAndMetadata
      |$doc
    """.stripMargin
  }
}

case class Delete(index: String, `type`: String, id: String) extends ESOperation with BulkSupport {
  override def bulkJson: String = {
    val actionAndMetadata = compact(render("delete" -> ("_index" -> index) ~ ("_type" -> `type`) ~ ("_id" -> id)))
    s"""
      |$actionAndMetadata
    """.stripMargin
  }
}

case class MultiGet(docs: Seq[Doc]) extends ESOperation

case class Get(index: String, `type`: String, id: String) extends ESOperation

sealed trait BulkSupport {
  def bulkJson: String
}

case class Bulk(actions: Seq[BulkSupport])
