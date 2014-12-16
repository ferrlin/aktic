package com.notik.sprastic

import com.notik.sprastic.client._

object Examples extends App {

  val client = SprasticClient()

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  implicit val timeout: FiniteDuration = 5 minutes

  import scala.util.{ Success, Failure }
  import com.notik.sprastic.api._

  /*  client.execute(Get("twitter", "tweet", "1")) onComplete {
    case Success(response) ⇒ println(response)
    case Failure(failure) ⇒ println(failure)
  }
  */

  client.execute(ESDelete("members", "member", ""))
  // client.execute(Index(""))
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
  // client.execute(Index("members", "member", doc, None, Some(Create)))
}
