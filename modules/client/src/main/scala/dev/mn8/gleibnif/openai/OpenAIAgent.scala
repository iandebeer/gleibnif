package dev.mn8.gleibnif.openai

import pureconfig.*
import pureconfig.generic.derivation.default.*

import io.circe.*
import io.circe.syntax.*

import sttp.client3.*
import sttp.client3.circe.*
import cats.implicits._

import cats.effect.IO
import dev.mn8.gleibnif.signal.SignalSimpleMessage
import cats.effect.Sync
import cats.implicits._
import cats.data.EitherT

case class APIConf(apiKey: String, orgId: String) derives ConfigReader:
  override def toString: String = s"APIConf(key: ${apiKey.toString}, orgId: ${orgId.toString})"

final case class OpenAIAgent():
  import OpenAIMessageCodec.openAIRequestEncoder
  import OpenAIMessageCodec.openAIResponseDecoder

  private val client = SimpleHttpClient()

  def getConf() = 
    val apiConf: APIConf = ConfigSource.default.at("openai-conf").load[APIConf]  match
      case Left(error) => 
        println(s"Error: $error")
        APIConf("", "" )
      case Right(conf) => conf
    println(s"API Conf: $apiConf")
    println(s"API Key: ${apiConf.apiKey}")  
    apiConf

  val apiConf= getConf() 

  def extractKeywords(message: SignalSimpleMessage): EitherT[IO, Exception, SignalSimpleMessage] =
    val openAIRequest = OpenAIRequest(prompt = s"Extract keywords from this text: ${message.text}").asJson.noSpaces
    val request = basicRequest
      .contentType("application/json")
      .header("Authorization", s"Bearer ${apiConf.apiKey}",replaceExisting = true)
      .body(openAIRequest)
      .response(asJson[OpenAIResponse])
      .post(uri"https://api.openai.com/v1/completions")
    val response = client.send(request)
    response.body match
      case Left(s:ResponseException[String, Error]) => 
        EitherT(IO.delay(Left(s)))
      case Right(openAIResponse) =>
        val keys = openAIResponse.choices.map(choice => choice.text)
        EitherT(IO.delay(Right(message.copy(keywords = keys))))

    

        
 

