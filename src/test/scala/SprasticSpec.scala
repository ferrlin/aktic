package com.notik.sprastic

import org.scalatest._
import org.scalatest.matchers._

abstract class UnitSpec extends FreeSpec
  with Matchers
  with OptionValues
  with Inside
  with Inspectors

class ESOperationsSpec extends UnitSpec {
  import com.notik.sprastic.api._
  import spray.httpx.RequestBuilding._

  "The Index case class" - {
    "when populated without an Id and criteria ie index=members;type=member;opType=Create" - {
      "should yield an expected HTTP request" in {
        val index = "members"
        val t = "member"
        val opType = Some(Create)
        val doc = """
      {
        "member":{
                "name" : {"type": "string", "index": "not_analyzed"},
                "age" : {"type": "integer"},
                "properties":{
                  "books": {
                    "type": "nested",
                    "properties": {
                      "author": {"type": "string"},
                      "borrowedOn": {"type": "date"}
                    }
                  }
                }
              }
      }
      """
        val indexMsg = ESIndex(index, t, doc, None, opType)

        val uri = s"${opType.map(op ⇒ s"?op_type=${op.value}").getOrElse("")}"
        indexMsg.httpRequest should be(Post(s"/$index/$t/$uri", doc))
      }
    }
  }

  "The Index case class" - {
    "when populated with an Id of 123 and criteria ie index=members;type=member;opType=Create" - {
      "should yield an expected HTTP request" in {
        val index = "members"
        val t = "member"
        val opType = Some(Create)
        val x = Some("123")
        val doc = """
      {
        "member":{
                "name" : {"type": "string", "index": "not_analyzed"},
                "age" : {"type": "integer"},
                "properties":{
                  "books": {
                    "type": "nested",
                    "properties": {
                      "author": {"type": "string"},
                      "borrowedOn": {"type": "date"}
                    }
                  }
                }
              }
      }
      """
        val indexMsg = ESIndex(index, t, doc, x, opType)
        val Some(sx) = x
        val uri = s"$sx${opType.map(op ⇒ s"?op_type=${op.value}").getOrElse("")}"
        indexMsg.httpRequest should be(Put(s"/$index/$t/$uri", doc))
      }
    }
  }
}