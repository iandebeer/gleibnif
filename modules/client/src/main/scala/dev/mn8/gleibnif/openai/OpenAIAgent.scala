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

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

case class APIConf(apiKey: String, orgId: String) derives ConfigReader:
  override def toString: String = s"APIConf(key: ${apiKey.toString}, orgId: ${orgId.toString})"

final case class OpenAIAgent()(using logger: Logger[IO] ):
  import OpenAIMessageCodec.openAIRequestEncoder
  import OpenAIMessageCodec.openAIResponseDecoder

  logger.info(s"OpenAIAgent starting...3") 
  private val client = SimpleHttpClient()
  def log[T](value: T)(using logger: Logger[IO]): IO[Unit] =
    logger.info(s"$value") 
  def getConf() = 
    val apiConf: APIConf = ConfigSource.default.at("openai-conf").load[APIConf]  match
      case Left(error) => 
        logger.error(s"$error") *> IO.pure(error) 
        APIConf("", "" )
      case Right(conf) => conf
    logger.info(s"API Conf: $apiConf") *> IO.unit
    logger.info(s"API Key: ${apiConf.apiKey}") 
    apiConf

  val apiConf= getConf() 

  def extractKeywords(message: SignalSimpleMessage): EitherT[IO, Exception, SignalSimpleMessage] =
    log("Extract keywords")

    val openAIRequest = OpenAIRequest(prompt = s"Extract keywords from this text: ${message.text}").asJson.noSpaces
    val request = basicRequest
      .contentType("application/json")
      .header("Authorization", s"Bearer ${apiConf.apiKey}",replaceExisting = true)
      .body(openAIRequest)
      .response(asJson[OpenAIResponse])
      .post(uri"https://api.openai.com/v1/completions")
    val curl = request.toCurl
    //println(s"curl: \n $curl")
    val response = client.send(request)
    log(s"Response: ${response.body.toString}")
    
    response.body match
      case Left(s:ResponseException[String, Error]) => 
        logger.error(s"Error: $s") *> IO.unit
        EitherT(IO.delay(Left(s)))
      case Right(openAIResponse) =>
        val keys = openAIResponse.choices.map(choice => choice.text)
        log(keys.mkString(","))
        EitherT(IO.delay(Right(message.copy(keywords = keys))))

    

        
 

