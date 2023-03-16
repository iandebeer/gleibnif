package dev.mn8.gleibnif

import munit.*
import io.circe.*
import io.circe.syntax.*

import io.circe.parser.*

import sttp.client3.*
import sttp.client3.circe.*
import io.circe.generic.auto._
import java.net.URI

import scala.concurrent.{ExecutionContext, Future}

val baseURL = "http://13.244.55.248:8080/prism-agent"
val apiKey = "kxr9i@6XgKBUxe%O"

val backend = HttpClientSyncBackend()

def publishDID(did: String): Unit = {
  // Publish the DID stored in Prism Agent's wallet to the VDR.
  val endpoint = s"did-registrar/dids/$did/publications"

  val url = uri"$baseURL/$endpoint"

  val request = basicRequest
    .post(url)
    .header("Content-Type", "application/json")
    .header("APIKey", apiKey)
  val response = request.send(backend)

  response.body match {
    case Left(error)  => throw new Exception(s"Error creating DID: $error")
    case Right(value) => println(s"DID created successfully: $value")
  }
}

def createUnpublishedDID(): Unit = {
  // Create unpublished DID and store it inside Prism Agent's wallet. The private keys of the DID is managed by Prism Agent. The DID can later be published to the VDR using publications endpoint.
  val endpoint = "did-registrar/dids"

  val documentTemplate = DocumentTemplate(
    Seq(
      PublicKey("key1", "authentication"),
      PublicKey("key2", "assertionMethod")
    ),
    Seq(
      Service("did:prism:test1", "LinkedDomains", Seq("https://test1.com")),
      Service("did:prism:test2", "LinkedDomains", Seq("https://test2.com"))
    )
  )

  val didRequest = CreateManagedDidRequest(documentTemplate)

  val url = uri"$baseURL/$endpoint"

  val request = basicRequest
    .post(url)
    .header("Content-Type", "application/json")
    .header("APIKey", apiKey)
    .body(didRequest)

  val backend = HttpClientSyncBackend()
  val response = request.send(backend)

  response.body match {
    case Left(error)  => throw new Exception(s"Error creating DID: $error")
    case Right(value) => println(s"DID created successfully: $value")
  }
}

class PrismClientSpec extends FunSuite {
  test("PrismClient should be able to create a DID") {
    createUnpublishedDID()
  }

  test("PrismClient should be able to publish a DID") {
    val testDID =
      "did:prism:28d8341f62b29054736450f8bc9cb8117792b87e4763f0a2fe4c1c5d18dd358f"
    publishDID(testDID)
  }
}

case class DocumentTemplate(publicKeys: Seq[PublicKey], services: Seq[Service])
case class PublicKey(id: String, purpose: String)
case class Service(id: String, `type`: String, serviceEndpoint: Seq[String])
case class CreateManagedDidRequest(documentTemplate: DocumentTemplate)
