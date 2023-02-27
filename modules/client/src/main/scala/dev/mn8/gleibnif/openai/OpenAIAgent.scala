package dev.mn8.gleibnif.openai

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
import dev.mn8.gleibnif.signal.SignalSimpleMessage


case class APIConf(apiKey: String, orgId: String) derives ConfigReader:
  override def toString: String = s"APIConf(key: ${apiKey.toString}, orgId: ${orgId.toString})"


final case class OpenAIAgent():
  import OpenAIMessageCodec.openAIRequestEncoder
  import OpenAIMessageCodec.openAIResponseDecoder

  val backend = HttpClientSyncBackend()
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

  def extractKeywords(message: SignalSimpleMessage): IO[SignalSimpleMessage] = 
    val openAIRequest = OpenAIRequest(prompt = s"Extract keywords from this text: ${message.text}").asJson.noSpaces
    println(s"Request: $openAIRequest")
    val request: Request[Either[ResponseException[String, Error], OpenAIResponse], Any] = basicRequest
     .contentType("application/json")
     .header("Authorization", s"Bearer ${apiConf.apiKey}",replaceExisting = true)
     .body(openAIRequest)
     .response(asJson[OpenAIResponse])
     .post(uri"https://api.openai.com/v1/completions")
    println(s"Request: $request")
    val response = request.send(backend)

    response.body match
      case Left(s:ResponseException[String, Error]) => 
        println(s"Error: ${s.getMessage()}")
        IO(message)
      case Right(openAIResponse) =>
        val keys = openAIResponse.choices.map(choice => choice.text)
        println(keys.mkString(","))
        IO(message.copy(keywords = keys))

        
 

