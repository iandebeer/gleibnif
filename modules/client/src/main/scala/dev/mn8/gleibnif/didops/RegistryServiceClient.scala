package dev.mn8.gleibnif.didops

import io.circe.*
import io.circe.syntax.*
import sttp.client3.*
import sttp.model.*
import sttp.client3.circe.*
import cats.implicits._

import io.circe.parser.*
import cats.effect.IO

 

import RegistryResponseCodec.registryResponseDecoder


final case class RegistryServiceClient(registryUrl: String, apiKey: String, document:String): 
  val backend = HttpClientSyncBackend()

//https://api.godiddy.com/0.1.0/universal-registrar/create?method=sov


  def createDID(method:String): IO[String] = 
    val exampleDID = "did:example:48e8cec"

    val url = uri"${registryUrl}create?method=$method"
    println(s"Creating DID with url: $url")
    println(s"apiKey $apiKey")
    println(s"document ${document.length}: $document")  

    val request: RequestT[Identity, Either[ResponseException[String, Error], RegistryResponse], Any] = basicRequest
      .post(url)
      .header("Content-Type", "application/json")
      .header("Authorization", s"Bearer ${apiKey}")
      .response(asJson[RegistryResponse])
      .body(document)

    val curl = request.toCurl
    request.headers.foreach(println)
    println(s"curl: \n $curl")

    val response: Response[Either[ResponseException[String, Error], RegistryResponse]] = request.send(backend) 

    response.body match
      case Left(error) =>
        println (s"Error creating DID: $error.")
        IO(exampleDID)
      case Right(registryResponse) =>
        println(s"DID created successfully: $registryResponse")
        IO(registryResponse.didState.did)
            