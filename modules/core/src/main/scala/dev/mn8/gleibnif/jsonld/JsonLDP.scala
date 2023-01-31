package dev.mn8.gleibnif.jsonld

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import io.circe.parser.*

import com.apicatalog.jsonld.JsonLd
import java.io.Reader
import com.apicatalog.jsonld.document.JsonDocument
import java.io.StringReader
import scala.io.Source
import java.net.URL
import java.net.URI
import scala.io.Codec
import io.circe.Json
import io.circe.ParsingFailure

case class JsonLDP(json: Json):
  val document = JsonDocument.of(StringReader(json.noSpaces))

  def expand() = 
    parse(JsonLd.
      expand(document)
      .get.getJsonObject(0).toString()) match
        case Left(failure) => Left(failure)
        case Right(json) => Right(JsonLDP(json))
      
  def compact(uri: URI)  =  
    parse(JsonLd.compact(document,uri)
      .compactToRelative(false)
      .get.toString()) match
        case Left(failure) => Left(failure)
        case Right(json) => Right(JsonLDP(json))

  def flatten()  = 
    parse(JsonLd.flatten(document).get.toString()) 
    match
        case Left(failure) => Left(failure)
        case Right(json) => Right(JsonLDP(json))

  def asRDF() = 
    parse(JsonLd.toRdf(document).get.toString()) 
    match
        case Left(failure) => Left(failure)
        case Right(json) => Right(JsonLDP(json))
