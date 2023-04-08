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

final case class RegistryServiceClient(registryUrl: String, apiKey: String): 
  private val client = SimpleHttpClient()
  def createDID(method:String, document:String): EitherT[IO,Exception,String] =

    val url = uri"${registryUrl}create?method=$method"
    val request: RequestT[Identity, Either[ResponseException[String, Error], RegistryResponse], Any] = basicRequest
      .post(url)
      .header("Content-Type", "application/json")
      .header("Authorization", s"Bearer ${apiKey}")
      .response(asJson[RegistryResponse])
      .body(document)

    //val curl = request.toCurl
    //request.headers.foreach(println)
    //println(s"curl: \n $curl")
    println("Create DID")

    EitherT(IO.delay(client.send(request).body.map(r => r.didState.did)))
