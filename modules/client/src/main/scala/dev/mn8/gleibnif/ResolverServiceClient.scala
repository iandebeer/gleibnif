package dev.mn8.gleibnif

import cats.effect.IO
import dev.mn8.gleibnif.didcomm.DIDCodec.decodeDIDDoc
import dev.mn8.gleibnif.didcomm.DIDDoc
import io.circe.*
import io.circe.parser.*
import sttp.client3.*
import sttp.client3.circe.*
import dev.mn8.gleibnif.didcomm.DIDCodec

case class ResolverServiceClient(resolverURI:String, apiKey: String,contentType: String = "application/did+ld+json"):

  def resolve(did: String): IO[Either[Exception,DIDDoc]] =
    val request =
      basicRequest.get(uri"$resolverURI$did")
      .header("Accept", contentType)
      .header("Authorization", s"Bearer ${apiKey}")
      .response(asJson[DIDDoc])
    val backend = HttpClientSyncBackend()
    val response = request.send(backend)
    IO.delay(response.body)


   
   

