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

final case class PrismClient(prismUrl: String, apiKey: String, documentTemplate: DocumentTemplate):
  private val backend = HttpClientSyncBackend()

  def createDID(): IO[Either[Exception,String]] = 
    val result = createUnpublishedDID() match 
      case Left(error) =>
        Left(new Exception(s"Error creating DID: $error"))
      case Right(jsonResponse) =>
        val parsedResponse = decode[CreateDIDResponse](jsonResponse).toOption
        parsedResponse.map(_.longFormDid) match
          case None =>
            Left(Exception("Could not parse longFormDid from response. Returning example did."))
          case Some(longDID) =>
            getPublishDIDResponse(longDID)
    IO.delay(result)
  

  private def createUnpublishedDID() = 
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

  private def publishDID(longFormDID: String): Either[String, String] = 
    // Publish the DID stored in Prism Agent's wallet to the VDR.
    val endpoint = s"did-registrar/dids/$longFormDID/publications"

    val url = uri"$prismUrl/$endpoint"

    val request = basicRequest
      .post(url)
      .header("Content-Type", "application/json")
      .header("APIKey", apiKey)

    val response = request.send(backend)

    response.body
  

  private def getPublishDIDResponse(longFormDID: String): Either[Exception,String] = 
    publishDID(longFormDID) match 
      case Left(error) =>
        Left(Exception(s"Error publishing DID: $error."))

      case Right(jsonResponse) =>
        decode[PublishedDIDResponse](jsonResponse).map(_.scheduledOperation.didRef)

      
        
    
  

case class DocumentTemplate(publicKeys: Seq[PublicKey], services: Seq[Service])
case class PublicKey(id: String, purpose: String)
case class Service(id: String, `type`: String, serviceEndpoint: Seq[String])
case class CreateManagedDidRequest(documentTemplate: DocumentTemplate)
case class ScheduledOperation(didRef: String, id: String)

case class CreateDIDResponse(longFormDid: String)
case class PublishedDIDResponse(scheduledOperation: ScheduledOperation)
