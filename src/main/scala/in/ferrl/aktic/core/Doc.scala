package in.ferrl.aktic.core

case class Docs(docs: Seq[Doc])
case class Doc(_index: String, _type: Option[String], _id: String)
