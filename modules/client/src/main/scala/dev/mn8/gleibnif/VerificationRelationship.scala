package dev.mn8.gleibnif

sealed trait VerificationRelationship

sealed trait Authentication 

sealed trait VerificationReference(ref: String) extends VerificationRelationship

sealed trait  VerificationInstance(
    id: String,
    `type`: String,
    controller: String,
    publicKeyMultibase: String
) extends VerificationRelationship 


case class KeyAgreementInstance(
    id: String,
    `type`: String,
    controller: String,
    publicKeyMultibase: String
)  extends VerificationInstance(id, `type`, controller, publicKeyMultibase)

case class KeyAgreementReference(ref: String) extends VerificationReference(ref)

case class AuthenticationInstance(
    id: String,
    `type`: String,
    controller: String,
    publicKeyMultibase: String
) extends VerificationInstance(id, `type`, controller, publicKeyMultibase)  with Authentication

case class AuthenticationReference(ref: String) extends VerificationReference(ref)  with Authentication