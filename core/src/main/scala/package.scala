package object aktic {
  object Message {
    import akka.actor.ActorRef

    case class WithData(data: ResponseDataAsString, target: ActorRef)
    case class WithError(err: ErrorMessage, target: ActorRef)
  }

  // Return types
  type ErrorMessage = String
  type ResponseDataAsString = String
}
