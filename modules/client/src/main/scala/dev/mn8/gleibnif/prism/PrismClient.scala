package dev.mn8.gleibnif.prism
import cats.effect.IO
import cats.implicits.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.*
import pureconfig.generic.derivation.default.*
import sttp.client3.ResponseException
import sttp.client3.UriContext
import sttp.client3.*
import sttp.client3.circe.*
import sttp.model.StatusCode
import sttp.model.Uri

final case class PrismClient(
    prismUrlString: String,
    apiKey: String,
    backendA: SttpBackend[IO, Any]
):
  val prismUrl = Uri
    .parse(prismUrlString)
    .getOrElse(throw new RuntimeException("Invalid Prism URL"))

  val sampleDid =
    "did:prism:28d8341f62b29054736450f8bc9cb8117792b87e4763f0a2fe4c1c5d18dd358f"

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  def log[T](value: T)(implicit logger: Logger[IO]): IO[Unit] =
    logger.info(s"PrismClient: $value")

  def createDID(
      documentTemplate: DocumentTemplate
  ): IO[Either[Exception, String]] =
    val result: IO[Either[Exception, IO[String]]] = createUnpublishedDID(
      documentTemplate
    ).map(r =>
      r match
        case Left(error) =>
          log(s"Error creating DID: $error")
          Left(error)
        case Right(unpublisheDidJsonResponse) =>
          val parsedResponse =
            decode[CreateDIDResponse](unpublisheDidJsonResponse).toOption

          val resultDID: IO[String] = parsedResponse.map(_.longFormDid) match
            case None =>
              log(
                s"Could not parse longFormDid from response. Returning example did."
              )
              IO.delay(sampleDid)
            case Some(longDID) =>
              getPublishDIDResponse(longDID).map(publishDidResponse =>
                publishDidResponse match
                  case Left(value) =>
                    log(
                      s"Error from getPublishDIDResponse in createDID. Returning example did."
                    )
                    sampleDid
                  case Right(value) =>
                    value
              )

          Right(resultDID)
    )
    val transformedResult: IO[Either[Exception, String]] = result.flatMap {
      case Left(exception) => IO.pure(Left(exception))
      case Right(ioString) =>
        ioString.map(Right(_)) // unwrap the IO[String] in Right side
    }
    transformedResult

  def createUnpublishedDID(
      documentTemplate: DocumentTemplate
  ): IO[Either[Exception, String]] =
    // Create unpublished DID and store it inside Prism Agent's wallet. The private keys of the DID is managed by Prism Agent. The DID can later be published to the VDR using publications endpoint.
    val endpoint = Seq("did-registrar", "dids")

    val didRequest = CreateManagedDidRequest(documentTemplate)

    val url = prismUrl.addPath(endpoint)

    val request = basicRequest
      .post(url)
      .header("Content-Type", "application/json")
      .header("APIKey", apiKey)
      .body(didRequest)

    val response = request.send(backendA)

    response.map(r =>
      r.body match
        case Left(value) =>
          log(s"Error creating unpublished DID: $value")
          Left(new Exception(value))
        case Right(r: String) =>
          log(s"created unpublished DID: $r")
          Right(r)
    )

  def publishDID(
      longFormDID: String
  ): IO[Either[Exception, String]] =
    // Publish the DID stored in Prism Agent's wallet to the VDR.
    val endpoint = Seq("did-registrar", "dids", longFormDID, "publications")

    val url = prismUrl.addPath(endpoint)

    val request = basicRequest
      .post(url)
      .header("Content-Type", "application/json")
      .header("APIKey", apiKey)

    val response = request.send(backendA)

    response.map(r =>
      r.body match
        case Left(value) =>
          log(s"Error creating published DID: $value")
          Left(new Exception(value))
        case Right(r: String) =>
          log(s"created published DID: $r")
          Right(r)
    )

  private def getPublishDIDResponse(
      longFormDID: String
  ): IO[Either[Exception, String]] =
    publishDID(longFormDID).map(eitherT =>
      eitherT match
        case Left(error: Exception) =>
          log(s"Error publishing DID: $error")
          Left(error)

        case Right(jsonResponse) =>
          decode[PublishedDIDResponse](jsonResponse).map(
            _.scheduledOperation.didRef
          )
    )

case class DocumentTemplate(publicKeys: Seq[PublicKey], services: Seq[Service])
case class PublicKey(id: String, purpose: String)
case class Service(id: String, `type`: String, serviceEndpoint: Seq[String])
case class CreateManagedDidRequest(documentTemplate: DocumentTemplate)
case class ScheduledOperation(didRef: String, id: String)

case class CreateDIDResponse(longFormDid: String)
case class PublishedDIDResponse(scheduledOperation: ScheduledOperation)
