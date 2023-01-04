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
          keyAgreement: Option[Set[KeyAgreement]] <- c
            .downField("didDocument")
            .downField("keyAgreement")
            .as[Option[Set[KeyAgreement]]]
          authentication: Option[Set[Authentication]] <- c
            .downField("didDocument")
            .downField("authentication")
            .as[Option[Set[Authentication]]]
          assertionMethod <- c
            .downField("assertionMethod")
            .as[Option[Set[Assertion]]]
          capabilityInvocation <- c
            // .downField("didDocument")
            .downField("capabilityInvocation")
            .as[Option[Set[CapabilityInvocation]]]
          capabilityDelegations <- c
            // .downField("didDocument")
            .downField("capabilityDelegations")
            .as[Option[Set[CapabilityDelegation]]]
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

  
  given decodeAssertionInstance: Decoder[AssertionInstance] =
    new Decoder[AssertionInstance]:
      final def apply(c: HCursor): Decoder.Result[AssertionInstance] =
        for {
          id <- c.downField("id").as[String]
          `type` <- c.downField("type").as[String]
          controller <- c.downField("controller").as[String]
          publicKeyMultibase <- c
            .downField("publicKeyMultibase")
            .as[String]
        } yield AssertionInstance(
          id,
          `type`,
          controller,
          publicKeyMultibase
        )

  given decodeAssertionReference: Decoder[AssertionReference] =
    new Decoder[AssertionReference]:
      final def apply(c: HCursor): Decoder.Result[AssertionReference] =
        for {
          value <- c.value.as[String]
        } yield AssertionReference(value) 

  given decodeAssertion: Decoder[Assertion] =
    decodeAssertionInstance.widen[Assertion] or decodeAssertionReference
      .widen[Assertion]


  given decodeKeyAgreementMethod: Decoder[KeyAgreementInstance] =
    new Decoder[KeyAgreementInstance]:
      final def apply(c: HCursor): Decoder.Result[KeyAgreementInstance] =
        for {
          id <- c.downField("id").as[String]
          `type` <- c.downField("type").as[String]
          controller <- c.downField("controller").as[String]
          publicKeyMultibase <- c
            .downField("publicKeyMultibase")
            .as[String]
        } yield KeyAgreementInstance(
          id,
          `type`,
          controller,
          publicKeyMultibase
        )
   
  given decodeKeyAgreementReference: Decoder[KeyAgreementReference] =
    new Decoder[KeyAgreementReference]:
      final def apply(c: HCursor): Decoder.Result[KeyAgreementReference] =
        for {
          value <- c.value.as[String]
        } yield KeyAgreementReference(value)

  given decodeKeyAgreement: Decoder[KeyAgreement] =
    decodeKeyAgreementReference.widen[KeyAgreement] or decodeKeyAgreementMethod
      .widen[KeyAgreement]


  given decodeCapabilityInvocationInstance
      : Decoder[CapabilityInvocationInstance] =
    new Decoder[CapabilityInvocationInstance]:
      final def apply(c: HCursor): Decoder.Result[CapabilityInvocationInstance] =
        for {
          id <- c.downField("id").as[String]
          `type` <- c.downField("type").as[String]
          controller <- c.downField("controller").as[String]
          publicKeyMultibase <- c
            .downField("publicKeyMultibase")
            .as[String]
        } yield CapabilityInvocationInstance(
          id,
          `type`,
          controller,
          publicKeyMultibase
        )

  given decodeCapabilityInvocationReference
      : Decoder[CapabilityInvocationReference] =
    new Decoder[CapabilityInvocationReference]:
      final def apply(
          c: HCursor
      ): Decoder.Result[CapabilityInvocationReference] =
        for {
          value <- c.value.as[String]
        } yield CapabilityInvocationReference(value)

  given decodeCapabilityInvocation : Decoder[CapabilityInvocation] =
    decodeCapabilityInvocationInstance
      .widen[CapabilityInvocation] or decodeCapabilityInvocationReference
      .widen[CapabilityInvocation]

  given decodeCapabilityDelegationInstance : Decoder[CapabilityDelegationInstance]=
    new Decoder[CapabilityDelegationInstance]:
      final def apply(c: HCursor): Decoder.Result[CapabilityDelegationInstance] =
        for {
          id <- c.downField("id").as[String]
          `type` <- c.downField("type").as[String]
          controller <- c.downField("controller").as[String]
          publicKeyMultibase <- c
            .downField("publicKeyMultibase")
            .as[String]
        } yield CapabilityDelegationInstance(
          id,
          `type`,
          controller,
          publicKeyMultibase
        )

  given decodeCapabilityDelegationReference : Decoder[CapabilityDelegationReference] =
    new Decoder[CapabilityDelegationReference]:
      final def apply(
          c: HCursor
      ): Decoder.Result[CapabilityDelegationReference] =
        for {
          value <- c.value.as[String]
        } yield CapabilityDelegationReference(value)

  given decodeCapabilityDelegation : Decoder[CapabilityDelegation] =
    decodeCapabilityDelegationInstance
      .widen[CapabilityDelegation] or decodeCapabilityDelegationReference
      .widen[CapabilityDelegation]
      


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
