package com.notik.sprastic.api

/** Elastic Search responses */
sealed trait Response {
  def index: Option[String] = None
  def `type`: Option[String] = None
  def id: Option[String] = None
  def version: Option[Int] = None
}
/* For Update Response, the created is expected to be `false`*/
case object IndexResponse extends Response {
  def created: Option[Boolean] = None
}
sealed trait Found {
  def found: Option[Boolean]
}
case object RetrieveResponse extends Response with Found {
  def found: Option[Boolean] = None
  def source: Option[String] = None
}
case object DeleteResponse extends Response with Found {
  def found: Option[Boolean] = None
}
case object ErrorResponse extends Response {
  def error: Option[String] = None
  def status: Option[Int] = None
}