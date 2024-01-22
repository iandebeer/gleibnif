package dev.mn8.gleibnif.jsonld

import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.document.JsonDocument
import io.circe.Json
import io.circe.ParsingFailure
import io.circe.parser.*

import java.io.Reader
import java.io.StringReader
import java.net.URI
import java.net.URL
import scala.io.Codec
import scala.io.Source
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

trait JsonLD:
  def expand(): Either[ParsingFailure, JsonLD]
  def compact(uri: URI): Either[ParsingFailure, JsonLD]
  def flatten(): Either[ParsingFailure, JsonLD]
  def asRDF(): Either[ParsingFailure, JsonLD]

object JsonLDP:
  def fromFile(file: String) =
    parse(Source.fromFile(file)(Codec.UTF8).mkString) match
      case Left(failure) => Left(failure)
      case Right(json)   => Right(JsonLDP(json))
  def fromURL(url: URL) =
    parse(Source.fromURL(url)(Codec.UTF8).mkString) match
      case Left(failure) => Left(failure)
      case Right(json)   => Right(JsonLDP(json))
  def fromURI(uri: URI) =
    parse(Source.fromURI(uri)(Codec.UTF8).mkString) match
      case Left(failure) => Left(failure)
      case Right(json)   => Right(JsonLDP(json))
  def fromString(string: String) =
    parse(string) match
      case Left(failure) => Left(failure)
      case Right(json)   => Right(JsonLDP(json))

case class JsonLDP(json: Json):
  val document = JsonDocument.of(StringReader(json.noSpaces))

  def expand() =
    parse(JsonLd.expand(document).get.getJsonObject(0).toString()) match
      case Left(failure) => Left(failure)
      case Right(json)   => Right(JsonLDP(json))

  def compact(uri: URI) =
    parse(
      JsonLd
        .compact(document, uri)
        .compactToRelative(false)
        .get
        .toString()
    ) match
      case Left(failure) => Left(failure)
      case Right(json)   => Right(JsonLDP(json))

  def flatten() =
    parse(JsonLd.flatten(document).get.toString()) match
      case Left(failure) => Left(failure)
      case Right(json)   => Right(JsonLDP(json))

  def asRDF() =
    parse(JsonLd.toRdf(document).get.toString()) match
      case Left(failure) => Left(failure)
      case Right(json)   => Right(JsonLDP(json))
  def fromRDF() =
    parse(JsonLd.fromRdf(document).get.toString()) match
      case Left(failure) => Left(failure)
      case Right(json)   => Right(JsonLDP(json))
