package com.notik.sprastic.api

import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import spray.httpx.RequestBuilding._
import spray.http.HttpRequest

sealed trait ESOperation {
  def httpRequest: HttpRequest
}

sealed trait OpType {
  def value: String
}
case object Create extends OpType {
  override def value: String = "create"
}

case class ESIndex(index: String,
  t: String,
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

  def httpRequest = id match {
    case Some(_) ⇒ Put(s"/$index/$t/$uri", document)
    case _ ⇒ Post(s"/$index/$t/$uri", document)
  }
}

case class ESUpdate(index: String,
  typ: String,
  document: String,
  id: String,
  version: Option[Int] = None)
  extends ESOperation with BulkSupport {
  override def bulkJson: String = {
    val actionAndMetadata = compact(render("update" -> ("_index" -> index) ~ ("_type" -> typ) ~ ("_id" -> id)))
    val doc = s""" {"doc": $document} """
    s"""
      |$actionAndMetadata
      |$doc
    """.stripMargin
  }
  def httpRequest = Post(s"/$index/$typ/$id${version.fold("")(v ⇒ s"?version=$v")}", document)
}

case class ESDelete(index: String, typ: String, id: String) extends ESOperation with BulkSupport {
  override def bulkJson: String = {
    val actionAndMetadata = compact(render("delete" -> ("_index" -> index) ~ ("_type" -> typ) ~ ("_id" -> id)))
    s"""
      |$actionAndMetadata
    """.stripMargin
  }
  def httpRequest = Delete(s"/$index/$typ/$id")
}

case class ESMultiGet(docs: Seq[Doc]) extends ESOperation {
  def httpRequest = Get("/_mget")
}

case class ESGet(index: String, typ: String, id: String) extends ESOperation {
  def httpRequest = Get(s"/$index/$typ/$id")
}

sealed trait BulkSupport {
  def bulkJson: String
}

case class ESBulk(actions: Seq[BulkSupport]) {
  lazy val json = actions.map(_.bulkJson).mkString("\n")
  def httpRequest = Post("/_bulk", json)
}
