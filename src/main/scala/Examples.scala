package com.notik.sprastic

import com.notik.sprastic.client._

object Examples extends App {

  val client = SprasticClient()

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  implicit val timeout: FiniteDuration = 5 minutes

  import scala.util.{ Success, Failure }
  import com.notik.sprastic.api._

  // Retrieving a document
  client.get("twitter", "tweet", "1") onComplete {
    case Success(response) ⇒ // do something with the response
    case Failure(ex) ⇒ // do nothing
  }

  val data = """
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
  // Indexing a document
  client.index("members", "member", data) onComplete {
    case Success(response) ⇒ // do something with the response
    case Failure(ex) ⇒ // do nothing
  }
  val id = "some arbitrary value"
  // Deleting a document
  client.delete("members", "member", id) onComplete {
    case Success(resp) ⇒ // do someting with the response
    case Failure(ex) ⇒ // do nothing
  }
}
