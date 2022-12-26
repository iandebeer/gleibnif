package dev.mn8.gleibnif

import io.circe.Decoder
//import summon.{Decoder => _, _}

sealed trait VerificationMaterial
  
case class VerificationMaterialJWK(
    crv: String,
    x: String,
    kty: String,
    kid: String
) extends VerificationMaterial

case class VerificationMaterialMultibase(
    value: String
) extends VerificationMaterial