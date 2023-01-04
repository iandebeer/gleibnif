package dev.mn8.gleibnif

import io.circe.Decoder.Result
import io.circe.*
import io.circe.Decoder
import cats.Applicative.ops.toAllApplicativeOps
import cats.*
import java.net.URI

//import summon.{Decoder => _, _}

object CirceDIDCodec:
  given decodeDIDDoc: Decoder[DIDDoc] =
    new Decoder[DIDDoc]:
      final def apply(c: HCursor): Decoder.Result[DIDDoc] =
        for {
          did <- c.downField("didDocument").downField("id").as[Option[String]]
          controller <- c
            .downField("didDocument")
            .downField("controller")
            .as[Option[String]]
          alsoKnownAs <- c
            .downField("didDocument")
            .downField("alsoKnownAs")
            .as[Option[Set[String]]]
          verificationMethod <- c
            .downField("didDocument")
            .downField("verificationMethod")
            .as[Option[Set[VerificationMethod]]]
          keyAgreement: Option[Set[VerificationRelationship]] <- c
            .downField("didDocument")
            .downField("keyAgreement")
            .as[Option[Set[VerificationRelationship]]]
          authentication: Option[Set[Authentication]] <- c
            .downField("didDocument")
            .downField("authentication")
            .as[Option[Set[Authentication]]]
          assertionMethod <- c
            .downField("assertionMethod")
            .as[Option[Set[VerificationRelationship]]]
          capabilityInvocation <- c
            // .downField("didDocument")
            .downField("capabilityInvocation")
            .as[Option[Set[VerificationRelationship]]]
          capabilityDelegations <- c
            // .downField("didDocument")
            .downField("capabilityDelegations")
            .as[Option[Set[VerificationRelationship]]]
          didCommServices <- c
            .downField("didDocument")
            .downField("service")
            .as[Option[Set[DIDCommService]]]
        } yield DIDDoc(
          did.getOrElse(""),
          controller,
          alsoKnownAs,
          verificationMethod,
          keyAgreement,
          authentication,
          assertionMethod,
          capabilityInvocation,
          capabilityDelegations,
          didCommServices
        )

  given decodeVerificationMethod: Decoder[VerificationMethod] =
    new Decoder[VerificationMethod]:
      final def apply(c: HCursor): Decoder.Result[VerificationMethod] =
        for {
          id <- c.downField("id").as[String]
          `type` <- c.downField("type").as[String]
          controller <- c.downField("controller").as[String]
          verificationMaterial <- c
            .downField("publicKeyJwk")
            .as[VerificationMaterialJWK]
            .orElse(
              c.downField("publicKeyMultibase")
                .as[VerificationMaterialMultibase]
            )

        } yield VerificationMethod(
          id,
          `type`,
          verificationMaterial,
          controller
        )

  given decodeVerificationMaterialJWK: Decoder[VerificationMaterialJWK] =
    Decoder.forProduct4("crv", "x", "kty", "kid")(VerificationMaterialJWK.apply)

  given decodeVerificationMaterialMultibase
      : Decoder[VerificationMaterialMultibase] =
    new Decoder[VerificationMaterialMultibase]:
      final def apply(
          c: HCursor
      ): Decoder.Result[VerificationMaterialMultibase] =
        for {
          value <- c.value.as[String]
        } yield VerificationMaterialMultibase(value)

  given decodeVerificationMaterial: Decoder[VerificationMaterial] =
    decodeVerificationMaterialJWK
      .widen[VerificationMaterial] or decodeVerificationMaterialMultibase
      .widen[VerificationMaterial]


  
    
  given decodeVerificationReReference: Decoder[KeyAgreementReference] =
    Decoder.forProduct1("ref")(KeyAgreementReference.apply)

  given decodeVerificationInstance: Decoder[KeyAgreementInstance] =
    Decoder.forProduct4("id", "type", "controller", "publicKeyMultibase")(
      KeyAgreementInstance.apply
    )

  given decodeVerificationRelationship: Decoder[VerificationRelationship] =
    decodeVerificationReReference
      .widen[VerificationRelationship] or decodeVerificationInstance
      .widen[VerificationRelationship]


  given decodeAuthenticationReference: Decoder[AuthenticationReference] =
    new Decoder[AuthenticationReference]:
      final def apply(
          c: HCursor
      ): Decoder.Result[AuthenticationReference] =
        for {
          value <- c.value.as[String]
        } yield AuthenticationReference(value)

  given decodeAuthenticationInstance: Decoder[AuthenticationInstance] =
    Decoder.forProduct4("id", "type", "controller", "publicKeyMultibase")(
      AuthenticationInstance.apply
    )

  given decodeAuthenticationRelationship: Decoder[Authentication] =
    decodeAuthenticationReference
      .widen[Authentication] or decodeAuthenticationInstance
      .widen[Authentication]


  given decodeDIDCommService: Decoder[DIDCommService] =
    new Decoder[DIDCommService]:
      final def apply(c: HCursor): Result[DIDCommService] =
        for {
          id <- c.downField("id").as[String]
          `type` <- c.downField("type").as[String]
          serviceEndpoint <- c.downField("serviceEndpoint").as[Set[URI]]
        } yield DIDCommService(
          id,
          `type`,
          serviceEndpoint
        )
