package dev.mn8.gleibnif.prism
import cats.effect.IO
import cats.implicits.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import pureconfig.*
import pureconfig.generic.derivation.default.*
import sttp.client3.*
import sttp.client3.circe.*

final case class PrismClient(prismUrl: String, apiKey: String):
  val backend = HttpClientSyncBackend()
  val exampleDID = "did:prism:48e8cec7eaf939e2bcd8d48dfa016dc338a1633289c430fc031c7ffd44738260"

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

  def createDID(): IO[String] = {
    val result: String = createUnpublishedDID() match {
      case Left(error) =>
        println(
          s"Error creating DID: $error.\nReturning example DID."
        )
        exampleDID

      case Right(jsonResponse) =>
        println(s"DID created successfully: $jsonResponse")

        val parsedResponse =
          decode[CreateDIDResponse](jsonResponse).toOption

        parsedResponse.map(_.longFormDid) match
          case None =>
            println(
              "Could not parse longFormDid from response. Returning example did."
            )
            exampleDID
          case Some(longDID) =>
            getPublishDIDResponse(longDID)
    }

    IO.delay(result)
  }

  def createUnpublishedDID() = {
    // Create unpublished DID and store it inside Prism Agent's wallet. The private keys of the DID is managed by Prism Agent. The DID can later be published to the VDR using publications endpoint.
    val endpoint = "did-registrar/dids"

    val didRequest = CreateManagedDidRequest(documentTemplate)

    val url = uri"$prismUrl/$endpoint"

    val request = basicRequest
      .post(url)
      .header("Content-Type", "application/json")
      .header("APIKey", apiKey)
      .body(didRequest)

    val response = request.send(backend)

    response.body
  }

  def publishDID(longFormDID: String): Either[String, String] = {
    // Publish the DID stored in Prism Agent's wallet to the VDR.
    val endpoint = s"did-registrar/dids/$longFormDID/publications"

    val url = uri"$prismUrl/$endpoint"

    val request = basicRequest
      .post(url)
      .header("Content-Type", "application/json")
      .header("APIKey", apiKey)

    val response = request.send(backend)

    response.body
  }

  def getPublishDIDResponse(longFormDID: String): String = {
    publishDID(longFormDID) match {
      case Left(error) =>
        println(
          s"Error publishing DID: $error. Returning example DID."
        )
        exampleDID
      case Right(jsonResponse) =>
        println(s"DID published successfully: $jsonResponse")

        val parsedResponse =
          decode[PublishedDIDResponse](jsonResponse).toOption

        parsedResponse.map(_.scheduledOperation.didRef).getOrElse {
          println(
            "Could not parse didRef from response. Returning example did."
          )
          exampleDID
        }
    }
  }

case class DocumentTemplate(publicKeys: Seq[PublicKey], services: Seq[Service])
case class PublicKey(id: String, purpose: String)
case class Service(id: String, `type`: String, serviceEndpoint: Seq[String])
case class CreateManagedDidRequest(documentTemplate: DocumentTemplate)
case class ScheduledOperation(didRef: String, id: String)

case class CreateDIDResponse(longFormDid: String)
case class PublishedDIDResponse(scheduledOperation: ScheduledOperation)
