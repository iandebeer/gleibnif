package dev.mn8.gleibnif.didops

import cats.data.EitherT
import cats.effect.IO
import cats.implicits.*
import dev.mn8.gleibnif.didops.RegistryResponseCodec.registryResponseDecoder
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import sttp.client3.*
import sttp.client3.circe.*
import sttp.model.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final case class RegistryServiceClient(registryUrl: String, apiKey: String):
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  def log[T](value: T)(implicit logger: Logger[IO]): IO[Unit] =
    logger.info(s"RegistryService: $value")
  private val client = SimpleHttpClient()
  def createDID(
      method: String,
      document: String,
      backendA: SttpBackend[IO, Any]
  ): IO[Either[ResponseException[String, Error], String]] =

    val url = uri"${registryUrl}create?method=$method"
    val request: RequestT[Identity, Either[
      ResponseException[String, Error],
      RegistryResponse
    ], Any] = basicRequest
      .post(url)
      .header("Content-Type", "application/json")
      .header("Authorization", s"Bearer ${apiKey}")
      .response(asJson[RegistryResponse])
      .body(document)

    val curl = request.toCurl
    request.headers.foreach(println)
    log("Creating DID...")
    // log(s"curl: \n $curl")
    request
      .send(backendA)
      .map(b =>
        b.body match
          case Left(s: ResponseException[String, Error]) =>
            log(s"Error: $s")
            log(s"curl: \n $curl")
            Left(s)
          case Right(r: RegistryResponse) =>
            log(s"created did: ${r.didState.did}")
            Right(r.didState.did)
      )
