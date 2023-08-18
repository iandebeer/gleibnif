package dev.mn8.gleibnif.didcomm

import cats.*
import cats.implicits.catsSyntaxApplicativeId
import cats.syntax.all.*
import io.circe.Decoder.Result
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.*

import java.net.URI
import dev.mn8.gleibnif.didcomm.{DIDCommMessage, DIDCommAttachment, DIDCommData}

object DIDCommCodec :
  given encodeDIDCommMessage: Encoder[DIDCommMessage] = new Encoder[DIDCommMessage] :
    final def apply(a: DIDCommMessage): Json = 
      Json.obj(
        ("id", Json.fromString(a.id)),
        ("type", Json.fromString(a.`type`)),
        ("from", a.from.map(f => Json.fromString(f.toString)).getOrElse(Json.Null)),
        ("to", a.to.map(t => Json.fromString(t.toString)).getOrElse(Json.Null)),
        ("created_time", a.createdTime.map(t => Json.fromString(t.toString)).getOrElse(Json.Null)),
        ("expires_time", a.expiresTime.map(t => Json.fromString(t.toString)).getOrElse(Json.Null)),
        ("body", Json.fromString(a.body)),
        ("attachments", Json.fromValues(a.attachments.getOrElse(List.empty[DIDCommAttachment]).map(encodeAttachment.apply))),
        
      )
  given encodeData: Encoder[DIDCommData] = new Encoder[DIDCommData] :
    final def apply(a: DIDCommData): Json = 
      Json.obj(
        ("jws", Json.fromString(a.jws.getOrElse(Json.Null.toString))),
        ("hash", Json.fromString(a.hash.getOrElse(Json.Null.toString))),
        ("links", Json.fromValues(a.links.getOrElse(List.empty[URI]).map(u => Json.fromString(u.toString)))),
        ("base64", Json.fromString(a.base64.getOrElse(Json.Null.toString))),
        ("json", io.circe.parser.parse(a.json.getOrElse(Json.Null.toString)) match 
          case Left(failure) => Json.Null
          case Right(json) => json)
      )

  given encodeAttachment: Encoder[DIDCommAttachment] = new Encoder[DIDCommAttachment] :
    final def apply(a: DIDCommAttachment): Json = 
      Json.obj(
        ("id", Json.fromString(a.id)),
        ("media_type", Json.fromString(a.mediaType.getOrElse(Json.Null.toString))),
        ("description", a.description.map(d => Json.fromString(d.toString)).getOrElse(Json.Null)),
        ("filename", a.filename.map(f => Json.fromString(f.toString)).getOrElse(Json.Null)),
        ("lastmod_time", a.lastmodTime.map(t => Json.fromString(t.toString)).getOrElse(Json.Null)),
        ("data", a.data.map(d => encodeData.apply(d)).getOrElse(Json.Null)),
        ("byte_count", a.byteCount.map(b => Json.fromInt(b)).getOrElse(Json.Null))
      )      
  given decodeDIDCommMessage: Decoder[DIDCommMessage] = new Decoder[DIDCommMessage] :
    final def apply(c: HCursor): Result[DIDCommMessage] = 
      for {
        id <- c.downField("id").as[String]
        `type` <- c.downField("type").as[String]
        from <- c.downField("from").as[Option[URI]]
        to <- c.downField("to").as[Option[List[URI]]]
        createdTime <- c.downField("created_time").as[Option[String]]
        expiresTime <- c.downField("expires_time").as[Option[String]]
        body <- c.downField("body").focus.map(s => s.noSpaces).pure[Result] //c.downField("body").as[String] // TODO: fix this (it's a bug in circe
        attachments <- c.downField("attachments").as[Option[List[DIDCommAttachment]]]
      } yield DIDCommMessage(id, `type`, from, to, None, None, createdTime, expiresTime, body.getOrElse(""), attachments) 

  
  given decodeData: Decoder[DIDCommData] = new Decoder[DIDCommData] :
    final def apply(c: HCursor): Result[DIDCommData] = 
      for {
        jws <- c.downField("jws").as[Option[String]]
        hash <- c.downField("hash").as[Option[String]]
        links <- c.downField("links").as[Option[List[String]]]
        base64 <- c.downField("base64").as[Option[String]]
        json <- c.downField("json").focus.map(s => s.noSpaces).pure[Result]
      } yield DIDCommData(jws, hash, links.map(l => l.map(u => URI.create(u))), base64, json) 

  given decodeAttachment: Decoder[DIDCommAttachment] = new Decoder[DIDCommAttachment] :
    final def apply(c: HCursor): Result[DIDCommAttachment] = 
      for {
        id <- c.downField("id").as[String]
        mediaType <- c.downField("media_type").as[Option[String]]
        description <- c.downField("description").as[Option[String]]
        filename <- c.downField("filename").as[Option[String]]
        lastmodTime <- c.downField("lastmod_time").as[Option[String]]
        data <- c.downField("data").as[Option[DIDCommData]]
        byteCount <- c.downField("byte_count").as[Option[Int]]
      } yield DIDCommAttachment(id, mediaType, description, filename, lastmodTime, data, byteCount) 
      