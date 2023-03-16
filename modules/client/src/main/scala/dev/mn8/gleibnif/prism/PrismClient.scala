package dev.mn8.gleibnif.prism
import pureconfig.*
import pureconfig.generic.derivation.default.*
import io.circe.*
import io.circe.syntax.*
import sttp.client3.*
import sttp.client3.circe.*
import io.circe._
import cats.implicits._

import io.circe.parser.*
import cats.effect.IO

final case class PrismClient(prismUrl: String, prismToken: String):
 
  val backend = HttpClientSyncBackend()

  def createDID(): IO[String] = IO.delay("did:prism:28d8341f62b29054736450f8bc9cb8117792b87e4763f0a2fe4c1c5d18dd358f")

  /*   for 
      prismRequest <- IO(PrismRequest(text = message.text).asJson.noSpaces)
      _ <- IO.println(s"Request: $prismRequest")
      request <- IO.blocking(basicRequest
        .contentType("application/json")
        .header("Authorization", s"Bearer ${prismToken}",replaceExisting = true)
        .body(prismRequest)
        .response(asJson[PrismResponse])
        .post(uri"${prismUrl}"))
      _ <- IO.println(s"Request: $request")
      response <-  IO.blocking(request.send(backend))

      result <-   response.body match
          case Left(s:ResponseException[String, Error]) => 
            println(s"Error: ${s.getMessage()}")
            IO(message)
          case Right(prismResponse) =>
            val keys = prismResponse.keywords
            println(keys.mkString(","))
            IO(message.copy(keywords = keys))
    yield result   */

