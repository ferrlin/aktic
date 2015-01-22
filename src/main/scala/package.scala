package object aktic {
  object Message {
    import akka.actor.ActorRef

    case class WithData(data: String, target: ActorRef)
    case class WithError(err: String, target: ActorRef)
  }

  // Return types
  type ErrorMessage = String
  type ResponseDataAsString = String
}
