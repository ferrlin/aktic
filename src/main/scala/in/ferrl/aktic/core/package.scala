package in.ferrl.aktic

package object core {
    case class Docs(docs: Seq[DocPath])
    case class DocPath(index: String, typ: String)
}