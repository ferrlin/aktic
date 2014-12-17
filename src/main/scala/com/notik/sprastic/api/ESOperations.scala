package com.notik.sprastic.api

import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import spray.httpx.RequestBuilding.{ Put ⇒ SPut }
import spray.httpx.RequestBuilding.{ Post ⇒ SPost }
import spray.httpx.RequestBuilding.{ Get ⇒ SGet }
import spray.httpx.RequestBuilding.{ Delete ⇒ SDelete }
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

case class Index(index: String,
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
    case Some(_) ⇒ SPut(s"/$index/$t/$uri", document)
    case _ ⇒ SPost(s"/$index/$t/$uri", document)
  }
}

case class Update(index: String,
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
  def httpRequest = SPost(s"/$index/$typ/$id${version.fold("")(v ⇒ s"?version=$v")}", document)
}

case class Delete(index: String, typ: String, id: String) extends ESOperation with BulkSupport {
  override def bulkJson: String = {
    val actionAndMetadata = compact(render("delete" -> ("_index" -> index) ~ ("_type" -> typ) ~ ("_id" -> id)))
    s"""
      |$actionAndMetadata
    """.stripMargin
  }
  def httpRequest = SDelete(s"/$index/$typ/$id")
}

case class MultiGet(docs: Seq[Doc]) extends ESOperation {
  def httpRequest = SGet("/_mget")
}

case class Get(index: String, typ: String, id: String) extends ESOperation {
  def httpRequest = SGet(s"/$index/$typ/$id")
}

sealed trait BulkSupport {
  def bulkJson: String
}

case class Bulk(actions: Seq[BulkSupport]) {
  lazy val json = actions.map(_.bulkJson).mkString("\n")
  def httpRequest = SPost("/_bulk", json)
}
