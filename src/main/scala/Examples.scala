import in.ferrl.aktic.Client

object Examples extends App {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  import scala.util.{ Success, Failure }
  import in.ferrl.aktic.api._
  implicit val timeout: FiniteDuration = 5 minutes

  val client = Client()

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
import java.util.Calendar
object IndexingExample extends App {

  object Util {
    implicit val dateformat = new java.text.SimpleDateFormat("yyyy-mm-dd")
  }
  // val memberCount = 1000 * 1000
  val memberCount = 100
  val client = Client()
  val index = "members"
  val typ = "member"
  val rand = new java.util.Random
  val averageTxCountPerMember = 10

  import Util._
  val futures = (0 until memberCount) map { id ⇒
    client.index(index, typ, createMember(id))
  }

  def createMember(id: Int): String = {
    val age = 12 + rand.nextInt(50)
    val bookCount = rand.nextInt(averageTxCountPerMember * 2)
    val books = (0 until bookCount) map createBook
    s"""
  { 
        "name": "Member $id", 
        "age": $age,
        "books": [ ${books.mkString(",")} ]
    }
  """
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