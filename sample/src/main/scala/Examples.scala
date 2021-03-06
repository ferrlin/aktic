import in.ferrl.aktic.Aktic

object Examples extends App {
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._
    import scala.util.{ Success, Failure }
    import in.ferrl.aktic.core._

    implicit val timeout: FiniteDuration = 5.minutes

    val client = Aktic()

    // Retrieving a document
    client.get("twitter", "tweet", "1") onComplete {
        case Success(response) ⇒ println(s"The response $response") // do something with the response
        case Failure(ex) ⇒ println(s"Error encountered: $ex") // do nothing
    }

    val data = """
  |{
  |  "member":{
  |          "name" : {"type": "string", "index": "not_analyzed"},
  |          "age" : {"type": "integer"},
  |          "properties":{
  |            "books": {
  |              "type": "nested",
  |              "properties": {
  |                "author": {"type": "string"},
  |                "borrowedOn": {"type": "date"}
  |              }
  |            }
  |          }
  |        }
  |}
  """.stripMargin
    // Indexing a document
    client.index("members", "member", data, None) onComplete {
        case Success(response) ⇒ println(s"Response $response") // do something with the response
        case Failure(ex) ⇒ println(s"Failure with $ex.getMessage") // do nothing
    }

    val updatedData = """
  |{
  |  "status" : "updated"
  |}
  """.stripMargin

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
    client.shutdown()
}

object IndexingExample extends App {

    import java.util.Calendar
    object Util {
        implicit val dateformat = new java.text.SimpleDateFormat("yyyy-mm-dd")
    }
    val memberCount = 100
    val client = Aktic()

    val index = "members"
    val typ = "member"
    val rand = new java.util.Random
    val averageTxCountPerMember = 10

    import Util._
    val futures = (0 until memberCount) map { id ⇒
        client.index(index, typ, createMember(id), None)
    }

    def createMember(id: Int): String = {
        val age = 12 + rand.nextInt(50)
        val bookCount = rand.nextInt(averageTxCountPerMember * 2)
        val books = (0 until bookCount) map createBook
        s"""
    |{ 
    |    "name": "Member $id", 
    |    "age": $age,
    |    "books": [ ${books.mkString(",")} ]
    |}
    """.stripMargin
    }

    def createBook(id: Int)(implicit dateFormat: java.text.SimpleDateFormat): String = {
        val authors = List("ranicki", "klein", "lessing")
        def randomAuthor(): String = authors(rand.nextInt(authors.size))
        def randomDate(): String = {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, 2014)
            cal.set(Calendar.DAY_OF_MONTH, 1 + rand.nextInt(28))
            cal.set(Calendar.MONTH, 1 + rand.nextInt(12))
            dateFormat.format(cal.getTime)
        }
        val author = randomAuthor()
        val date = randomDate()
        s"""{"id":$id,"author":"$author","borrowedOn":"$date"}"""
    }
}

object CreateRetrieveDeleteFlowExample extends App {

    import scala.concurrent.Future
    import akka.http.scaladsl.model.HttpResponse
    import scala.util.{ Failure, Success }

    val client = Aktic()
    val index = "CRDFlow".toLowerCase
    val typ = "Example".toLowerCase

    // automatically inject document path to our operations
    import in.ferrl.aktic.core.DocPath
    implicit val docPath = DocPath(index, typ)

    // start indexing our entries
    indexEntries()

    Thread.sleep(1000)

    import scala.concurrent.ExecutionContext.Implicits.global
    import argonaut._, Argonaut._

    retrieveAll onComplete {
        case Success(data) ⇒ {
            val jsonString = data
            val json = Parse.parseOption(jsonString)
            val hits2 = hits2Lens.get(json.get)
            val ids = hits2.get.flatMap(hits2ArrayIdLens.get)
            // We then delete the ids extracted 
            // deleteEntries(ids)

            Thread.sleep(1000)

            // close our client
            // client.shutdown
        }
        case Failure(x) ⇒ println(s"Failed $x")
    }

    client.shutdown()

    lazy val hits2Lens = jObjectPL >=>
        jsonObjectPL("hits") >=>
        jObjectPL >=>
        jsonObjectPL("hits") >=>
        jArrayPL

    lazy val hits2ArrayIdLens = jObjectPL >=>
        jsonObjectPL("_id") >=>
        jStringPL

    def indexEntries(): Unit = {
        (1 to 10) map { id ⇒
            // client.index(index, typ, """{ "type":"testing" }""", None)
            val id = None
            client.index(id, """{ "type":"testing" }""") onComplete {
                case Success(response) ⇒ println(s"After indexing the result is -> $response")
                case Failure(err) ⇒ // do nothing
            }
        }
    }

    def retrieveAll: Future[String] = {
        println("Client to Get All documents")
        client.search(index, List("size=20"))
    }

    def deleteEntries(entryIds: Seq[String]): Unit = {
        // individually delete the document with this id .. must have batch deletion
        // for (id ← entryIds) client.delete(index, typ, id)
        for (id ← entryIds) client.delete(id)
    }
}