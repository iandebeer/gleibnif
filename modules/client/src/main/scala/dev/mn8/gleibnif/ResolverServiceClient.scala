package dev.mn8.gleibnif

import sttp.client3.*
import sttp.client3.circe.*

import io.circe.*
import io.circe.parser.*
import dev.mn8.gleibnif.DIDDoc
import dev.mn8.gleibnif.DIDCodec.decodeDIDDoc
import cats.effect.IO

case class ResolverServiceClient(resolverURI:String, apiKey: String,contentType: String = "application/did+ld+json"):

  def resolve(did: String): IO[Either[ResolverError,DIDDoc]] =
    val request =
      basicRequest.get(uri"$resolverURI$did")
      .header("Accept", contentType)
      .header("Authorization", s"Bearer ${apiKey}")
      .response(asJson[DIDDoc])
    val backend = HttpClientSyncBackend()
    val response = request.send(backend)
   
    response.body match 
      case Left(e) => 
        println("Error: " + e)
        IO.delay(Left(ResolverError("Error: " + e)))

      case Right(b) => 
        println(s"Success! ${response.toString}")
        println("Response: " + b)
        IO.delay(Right(b))

  def resolveToJson(did: String): IO[Either[ResolverError,String]] =
    val request: RequestT[Identity, Either[String, String], Any] =
      basicRequest.get(uri"$resolverURI$did")
      .header("Accept", contentType)
      .header("Authorization", s"Bearer ${apiKey}")
      //.response(asJson[DIDDoc])
    val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()
    val response: Response[Either[String, String]] = request.send(backend)
    response.body match 
      case Left(e) => 
        println("Error: " + e)
        IO.delay(Left(ResolverError("Error: " + e)))

      case Right(b) => 
        parse(b) match
          case Left(e) => 
            println("Error: " + e)
            IO.delay(Left(ResolverError("Error: " + e)))
          case Right(json) => 
            println("JSon: " + json.spaces2)
            json.as[DIDDoc] match
              case Left(e) => 
                println("Error: " + e)
                IO.delay(Left(ResolverError("Error: " + e)))
              case Right(didDoc) => 
                println("DIDDoc: " + didDoc)
                IO.delay(Right(didDoc))
            IO.delay(Right(json.spaces2))
        //println("Response: " + b)
        IO.delay(Right(b))
   
   

case class ResolverError(message: String) extends Exception(message)
