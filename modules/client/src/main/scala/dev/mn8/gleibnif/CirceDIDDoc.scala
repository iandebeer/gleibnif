package dev.mn8.gleibnif

import io.circe.Decoder.Result
import io.circe.*
import io.circe.Decoder
import cats.Applicative.ops.toAllApplicativeOps


//import summon.{Decoder => _, _}

object CirceDIDCodec:

  given decodeDIDDoc : Decoder[DIDDoc] =
    new Decoder[DIDDoc]:
      final def apply(c: HCursor): Decoder.Result[DIDDoc] =
        for {
          did <- c.downField("didDocument").downField("id").as[Option[String]]
          alsoKnownAs <- c
            .downField("didDocument")
            .downField("alsoKnownAs")
            .as[Option[List[String]]]
          keyAgreements <- c
            .downField("didDocument")
            .downField("keyAgreement")
            .as[Option[List[String]]]
          authentications <- c
            .downField("didDocument")
            .downField("authentication")
            .as[Option[List[String]]]
          verificationMethods <- c
            .downField("didDocument")
            .downField("verificationMethod")
            .as[Option[List[VerificationMethod]]]
          didCommServices <- c
            .downField("didDocument")
            .downField("didCommService")
            .as[Option[List[DIDCommService]]]
        } yield DIDDoc(
          did.getOrElse(""),
          alsoKnownAs.getOrElse(List.empty),
          keyAgreements.getOrElse(List.empty),
          authentications.getOrElse(List.empty),
          verificationMethods.getOrElse(List.empty),
          didCommServices.getOrElse(List.empty)
        )

  given decodeVerificationMethod: Decoder[VerificationMethod] =
    new Decoder[VerificationMethod]:
      final def apply(c: HCursor): Decoder.Result[VerificationMethod] =
        for {
          id <- c.downField("id").as[String]
          controller <- c.downField("controller").as[String]
          `type` <- c.downField("type").as[String]
          verificationMaterial <- c
            .downField("publicKeyBase58")
            .as[Option[VerificationMaterial]]
        } yield VerificationMethod(
          id,
          VerificationMethodType.fromString(`type`),
          verificationMaterial.getOrElse(VerificationMaterialMultibase("")

          ),
          controller
        )

  
  given decodeVerificationMaterialJWK: Decoder[VerificationMaterialJWK] =
    Decoder.forProduct4("crv","x","ktyy","kid")(VerificationMaterialJWK.apply)
    

  given decodeVerificationMaterialMultibase: Decoder[VerificationMaterialMultibase] =
    Decoder.forProduct1("value")(VerificationMaterialMultibase.apply)

  given decodeVerificationMaterial: Decoder[VerificationMaterial] =
    decodeVerificationMaterialJWK.widen[VerificationMaterial] or decodeVerificationMaterialMultibase.widen[VerificationMaterial]

  given decodeDIDCommService: Decoder[DIDCommService] =
    new Decoder[DIDCommService]:
      final def apply(c: HCursor): Result[DIDCommService] =
        for {
          id <- c.downField("id").as[String]
          serviceEndpoint <- c.downField("serviceEndpoint").as[String]
          routingKeys <- c.downField("routingKeys").as[List[String]]
          accept <- c.downField("accept").as[List[String]]
        } yield DIDCommService(
          id,
          serviceEndpoint,
          routingKeys,
          accept
        )
