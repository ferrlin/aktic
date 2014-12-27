package com.notik.sprastic

import com.notik.sprastic.client._
// import 

object Examples extends App {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  implicit val timeout: FiniteDuration = 5 minutes

  import scala.util.{ Success, Failure }
  import com.notik.sprastic.api._

  val client = SprasticClient()
  // Retrieving a document
  client.get("twitter", "tweet", "1") onComplete {
    case Success(response) ⇒ println(s"The response $response") // do something with the response
    case Failure(ex) ⇒ println(s"Error encountered: $ex") // do nothing
  }

  // client.get("twitter", "tweet", "2") onSuccess {
  // println(s"Attempting to get tweet with id = 2")
  // }

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
    case Success(response) ⇒ println(s"Response $response") // do something with the response
    case Failure(ex) ⇒ println(s"Failure with $ex.getMessage") // do nothing
  }

  val updatedData = """
  {
  	"status" : "updated"
  }
  """
  client.update("members", "member", updatedData, "AUqBqA7Z6ldZWrFbDiVE") onComplete {
    case Success(res) ⇒ println(s"Response with: $res")
    case Failure(ex) ⇒ println(s"Failure with $ex.getMessage")
  }
  // val id = "some arbitrary value"
  // Deleting a document
  // client.delete("members", "member", id) onComplete {
  // case Success(resp) ⇒ // do someting with the response
  // case Failure(ex) ⇒ // do nothing
  // }
  Thread.sleep(5000)
  // client.shutdown()
}
