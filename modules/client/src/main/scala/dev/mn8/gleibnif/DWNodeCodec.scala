package dev.mn8.gleibnif

import cats.Applicative.ops.toAllApplicativeOps
import cats.*
import io.circe.Decoder
import io.circe.Decoder.Result
import io.circe.Encoder
import io.circe.Json
import io.circe.*

import java.net.URI

object DWNodeCodec:

  given encodeDWNodeMessage: Encoder[DWNodeMessage] =
    new Encoder[DWNodeMessage]:
      final def apply(a: DWNodeMessage): Json =
        Json.obj(
          (
            "messages",
            Json.fromValues(a.messages.map(encodeDWNodeRequestMessage.apply))
          )
        )

  given encodeDWNodeRequestMessage: Encoder[DWNodeRequestMessage] =
    new Encoder[DWNodeRequestMessage]:
      final def apply(a: DWNodeRequestMessage): Json =
        Json.obj(
          ("recordId", Json.fromString(a.recordId)),
          ("data", Json.fromString(a.data.getOrElse(Json.Null.toString))),
          (
            "descriptor",
            Json.obj(
              ("method", Json.fromString(a.descriptor.method)),
              ("dataCid", Json.fromString(a.descriptor.dataCid)),
              ("dataFormat", Json.fromString(a.descriptor.dataFormat))
            )
          ),
          (
            "processing",
            Json.obj(
              ("nonce", Json.fromString(a.processing.nonce)),
              ("author", Json.fromString(a.processing.author)),
              ("recipient", Json.fromString(a.processing.recipient))
            )
          ),
          (
            "attestations",
            Json.fromValues(
              a.attestations
                .getOrElse(List.empty[DWNodeAttestation])
                .map(encodeAttestation.apply)
            )
          ),
          (
            "authorizations",
            Json.fromValues(
              a.authorizations
                .getOrElse(List.empty[DWNodeAuthorization])
                .map(encodeAuthorization.apply)
            )
          )
        )

  given encodeAttestation: Encoder[DWNodeAttestation] =
    new Encoder[DWNodeAttestation]:
      final def apply(a: DWNodeAttestation): Json =
        Json.obj(
          ("payload", Json.fromString(a.payload)),
          (
            "signatures",
            Json.fromValues(a.signatures.map(encodeSignature.apply))
          )
        )

  given encodeAuthorization: Encoder[DWNodeAuthorization] =
    new Encoder[DWNodeAuthorization]:
      final def apply(a: DWNodeAuthorization): Json =
        Json.obj(
          ("payload", Json.fromString(a.payload)),
          (
            "signatures",
            Json.fromValues(a.signatures.map(encodeSignature.apply))
          )
        )

  given encodeSignature: Encoder[DWNodeSignature] =
    new Encoder[DWNodeSignature]:
      final def apply(a: DWNodeSignature): Json =
        Json.obj(
          ("protected", Json.fromString(a.`protected`)),
          ("signature", Json.fromString(a.signature))
        )

  given decodeDWNodeMessage: Decoder[DWNodeMessage] =
    new Decoder[DWNodeMessage]:
      final def apply(c: HCursor): Result[DWNodeMessage] =
        for messages <- c.downField("messages").as[List[DWNodeRequestMessage]]
        yield DWNodeMessage(messages)

  given decodeDWNodeRequestMessage: Decoder[DWNodeRequestMessage] =
    new Decoder[DWNodeRequestMessage]:
      final def apply(cur: HCursor): Result[DWNodeRequestMessage] =
        val c = cur.downField(("messages")).success match
          case Some(c) => c
          case None    => cur
        for {
          recordId   <- c.downField("recordId").as[String]
          data       <- c.downField("data").as[Option[String]]
          descriptor <- c.downField("descriptor").as[DWNodeDescriptor]
          processing <- c.downField("processing").as[DWNodeProcessing]
          attestations <- c
            .downField("attestations")
            .as[Option[List[DWNodeAttestation]]]
          authorizations <- c
            .downField("authorizations")
            .as[Option[List[DWNodeAuthorization]]]
        } yield DWNodeRequestMessage(
          recordId,
          data,
          processing,
          descriptor,
          attestations,
          authorizations
        )

  given decodeDWNodeDescriptor: Decoder[DWNodeDescriptor] =
    new Decoder[DWNodeDescriptor]:
      final def apply(c: HCursor): Result[DWNodeDescriptor] =
        for
          method     <- c.downField("method").as[String]
          dataCid    <- c.downField("dataCid").as[String]
          dataFormat <- c.downField("dataFormat").as[String]
        yield DWNodeDescriptor(method, dataCid, dataFormat)

  given decodeDWNodeProcessing: Decoder[DWNodeProcessing] =
    new Decoder[DWNodeProcessing]:
      final def apply(c: HCursor): Result[DWNodeProcessing] =
        for
          nonce     <- c.downField("nonce").as[String]
          author    <- c.downField("author").as[String]
          recipient <- c.downField("recipient").as[String]
        yield DWNodeProcessing(nonce, author, recipient)

  given decodeDWNodeAttestation: Decoder[DWNodeAttestation] =
    new Decoder[DWNodeAttestation]:
      final def apply(c: HCursor): Result[DWNodeAttestation] =
        for
          payload    <- c.downField("payload").as[String]
          signatures <- c.downField("signatures").as[List[DWNodeSignature]]
        yield DWNodeAttestation(payload, signatures)

  given decodeDWNodeAuthorization: Decoder[DWNodeAuthorization] =
    new Decoder[DWNodeAuthorization]:
      final def apply(c: HCursor): Result[DWNodeAuthorization] =
        for
          payload    <- c.downField("payload").as[String]
          signatures <- c.downField("signatures").as[List[DWNodeSignature]]
        yield DWNodeAuthorization(payload, signatures)

  given decodeDWNodeSignature: Decoder[DWNodeSignature] =
    new Decoder[DWNodeSignature]:
      final def apply(c: HCursor): Result[DWNodeSignature] =
        for
          `protected` <- c.downField("protected").as[String]
          signature   <- c.downField("signature").as[String]
        yield DWNodeSignature(`protected`, signature)
