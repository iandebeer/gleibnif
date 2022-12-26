package dev.mn8.gleibnif

//import org.didcommx.didcomm.common.VerificationMaterial
//import org.didcommx.didcomm.common.VerificationMethodType
import org.didcommx.didcomm.exceptions.DIDDocException
import org.didcommx.didcomm.exceptions.DIDUrlNotFoundException
import io.circe.*
import foundation.identity.did.VerificationRelationships


//import foundation.identity.did.VerificationMethod

/** DID DOC (https://www.w3.org/TR/did-core/#dfn-did-documents)
  * @property
  *   did a DID for the given DID Doc
  * @property
  *   keyAgreements Key IDs (DID URLs) of all verification methods from the
  *   'keyAgreement' verification relationship in this DID DOC. See
  *   https://www.w3.org/TR/did-core/#verification-methods.
  * @property
  *   authentications Key IDs (DID URLs) of all verification methods from the
  *   'authentication' verification relationship in this DID DOC. See
  *   https://www.w3.org/TR/did-core/#authentication.
  * @property
  *   verificationMethods Returns all local verification methods including
  *   embedded to key agreement and authentication sections. See
  *   https://www.w3.org/TR/did-core/#verification-methods.
  * @property
  *   didCommServices All services of 'DIDCommMessaging' type in this DID DOC.
  *   Empty list is returned if there are no services of 'DIDCommMessaging'
  *   type. See https://www.w3.org/TR/did-core/#services and
  *   https://identity.foundation/didcomm-messaging/spec/#did-document-service-endpoint.
  */
case class DIDDoc(
    did: String,
    alsoKnownAs: List[String],
    keyAgreements: List[String],
    authentications: List[String],
    verificationMethods: List[VerificationMethod],
    didCommServices: List[DIDCommService]) :

  // fun findVerificationMethod(id: String): VerificationMethod = verificationMethods.find { it.id == id } ?: throw DIDUrlNotFoundException(id, did)
  def findVerificationMethod(
      id: String
  ): Either[DIDUrlNotFoundException, VerificationMethod] =
    verificationMethods.find(_.id == id) match
      case Some(v: VerificationMethod) => Right(v)
      case None                        => Left(DIDUrlNotFoundException(id, did))

  def findDIDCommService(id: String): Either[DIDDocException, DIDCommService] =
    didCommServices.find(_.id == id) match
      case Some(v: DIDCommService) => Right(v)
      case None => Left(DIDDocException("DIDComm service not found"))





/** DID DOC Verification method. It can be used in such verification
  * relationships as Authentication, KeyAgreement, etc. See
  * https://www.w3.org/TR/did-core/#verification-methods.
  */
case class VerificationMethod(
    id: String,
    `type`: VerificationMethodType,
    verificationMaterial: VerificationMaterial,
    controller: String
)


/** DID DOC Service of 'DIDCommMessaging' type. see
  * https://www.w3.org/TR/did-core/#services and
  * https://identity.foundation/didcomm-messaging/spec/#did-document-service-endpoint.
  *
  * @property
  *   id Service's 'id' field
  * @property
  *   serviceEndpoint A service endpoint. It can be either a URI to be used for
  *   transport or a mediator's DID in case of alternative endpoints.
  * @property
  *   routingKeys A possibly empty ordered array of strings referencing keys to
  *   be used when preparing the message for transmission.
  * @property
  *   accept A possibly empty ordered array of strings representing accepted
  *   didcomm specification versions.
  */
case class DIDCommService(
    id: String,
    serviceEndpoint: String,
    routingKeys: List[String],
    accept: List[String]
)

sealed trait VerificationRelationship

case class VerificationReference(ref: String) extends VerificationRelationship
case class VerificationInstance(
    id: String,
    `type`: VerificationMethodType,
    controller: String,
    publicKeyMultibase: String
) extends VerificationRelationship

case class VerificationRelationships(
    authentication: List[VerificationRelationship],
    keyAgreement: List[VerificationRelationship],
    assertionMethod: List[VerificationRelationship],
    capabilityInvocation: List[VerificationRelationship],
    capabilityDelegation: List[VerificationRelationship]
)
