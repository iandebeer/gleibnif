package dev.mn8.gleibnif

import munit.*
import io.circe.*
import io.circe.syntax.*

import io.circe.parser.*
import dev.mn8.gleibnif.DIDCommMessage
import dev.mn8.gleibnif.DIDCommCodec.decodeDIDCommMessage
import dev.mn8.gleibnif.DIDCommCodec.encodeDIDCommMessage

import sttp.client3.*
import io.circe.generic.auto._
import java.net.URI

case class PublishDIDRequest(did: String, verkey: String)


import scala.concurrent.{ExecutionContext, Future}

val baseURL = "http://13.244.55.248:8080/prism-agent"

def publishDID(did: String): Unit  = {
    val requestBody = PublishDIDRequest(did, "v1").asJson
    
    val apiKey = "kxr9i@6XgKBUxe%O"
    
    val url = uri"${baseURL}/did-registrar/dids/${did}/publications"
    
    val request = basicRequest
    .post(url)
    .header("Content-Type", "application/json")
    .header("APIKey", apiKey)
    
    val backend = HttpClientSyncBackend()
    val response = request.send(backend)    
    
    response.body match {
        case Left(error) => throw new Exception(s"Error creating DID: $error")
        case Right(value) => println(s"DID created successfully: $value")
    }
}

class PrismClientSpec extends FunSuite {
    test("PrismClient should be able to publish a DID") {
        val testDID = "did:prism:28d8341f62b29054736450f8bc9cb8117792b87e4763f0a2fe4c1c5d18dd358f"
        publishDID(testDID)
    }
}