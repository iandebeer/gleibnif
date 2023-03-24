package dev.mn8.gleibnif

import sttp.client3.*
import sttp.client3.circe.*

import io.circe.*
import io.circe.parser.*
import dev.mn8.gleibnif.DIDDoc
import dev.mn8.gleibnif.DIDCodec.decodeDIDDoc

case class ResolverServiceClient(resolverURI:String):

  def resolve(did: String): Either[ResolverError,DIDDoc] =
    val request =
      basicRequest.get(uri"$resolverURI$did")
    val backend = HttpClientSyncBackend()
    val response = request.send(backend)
   
    response.body match 
      case Left(e) => 
        println("Error: " + e)
        Left(ResolverError("Error: " + e))

      case Right(b) => 
        parse(b) match 
        case Left(failure) => 
          println("Error: " + failure)
          Left(ResolverError("Error: " + failure))
        case Right(json) => 
          println("Success: " + json.spaces2)
          json.as[DIDDoc] match 
            case Left(e) => 
              println("Error: " + e)
              Left(ResolverError("Error: " + e))
            case Right(d) => 
              println("Success: " + d)
              Right(d)  

case class ResolverError(message: String) extends Exception(message)
