package dev.mn8.gleibnif.signal


import cats.data.EitherT
import cats.effect.IO
import cats.implicits.*
import dev.mn8.gleibnif.signal.SignalMessageCodec.signalMessageDecoder
import dev.mn8.gleibnif.signal.SignalMessageCodec.signalSendMessage
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import pureconfig.*
import pureconfig.generic.derivation.default.*
import sttp.client3.*
import sttp.client3.circe.*
import sttp.model.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

case class SignalConfig(
  signalUrl: String,
  signalUser: String,
  signalPhone: String,
  signalTimeout: Int = 5
) derives ConfigReader:
  override def toString: String = s"SignalConfig(url: ${signalUrl.toString}, user: ${signalUser.toString}, phone: ${signalPhone.toString})"

case class SignalBot(backend: SttpBackend[IO, Any]):
  type ErrorOr[A] = EitherT[IO, Exception, A]
  def getConf() = 
    val signalConf: SignalConfig = ConfigSource.default.at("signal-conf").load[SignalConfig]  match
      case Left(error) => 
        SignalConfig("", "", "" )
      case Right(conf) => conf
    signalConf

  val signalConf= getConf() 
  

  def init(): Unit = ()
  
  def register(voiceMode:Boolean): IO[Either[Exception, String]] = 
    val request = basicRequest.contentType("application/json").body(s"""{"use_voice": $voiceMode}""").post(
      uri"${signalConf.signalUrl}/register/${signalConf.signalPhone}")
    val response = request.send(backend) 
    response.map(c => c.code match
      case s:StatusCode if s.isSuccess => 
        Right(s"Signalbot verify: $s ")
      case s:StatusCode =>
        Left(new Exception(s"Signalbot Send: $s ")))
    
  def verify(pin:String): IO[Either[Exception, String]]  = 
    val request = basicRequest.contentType("application/json").body(s"""{"pin": $pin""").post(
      uri"${signalConf.signalUrl}/verify/${signalConf.signalPhone}")
    val response = request.send(backend) 
    response.map(c => c.code match
      case s:StatusCode if s.isSuccess => 
        Right(s"Signalbot verify: $s ")
      case s:StatusCode =>
        Left(new Exception(s"Signalbot Send: $s ")))

  def send(message: SignalSendMessage): IO[Either[Exception, String]] =
    val request = basicRequest.contentType("application/json").body(message.asJson.noSpaces).post(
      uri"${signalConf.signalUrl}/v2/send")
     //val curl = request.toCurl
    // request.headers.foreach(println)
    // println(s"curl: \n $curl")
    val response = request.send(backend)
    response.map(c => c.code match
      case s:StatusCode if s.isSuccess => 
        Right(s"Signalbot Send: $s - Message sent")
      case s:StatusCode =>
        Left(new Exception(s"Signalbot Send: $s ")))

  def receive(): IO[Either[ResponseException[String, Error], List[SignalSimpleMessage]]] = 
    val request = basicRequest.contentType("application/json").
      response(asJson[List[SignalMessage]]).
      get(uri"${signalConf.signalUrl}/v1/receive/${signalConf.signalPhone}?timeout=${signalConf.signalTimeout}")
   // val messages: EitherT[IO, ResponseException[String, Error], List[SignalMessage]] = EitherT(IO(request.send(backendA).map(b =>
     //val curl = request.toCurl
    // request.headers.foreach(println)
     //println(s"curl: \n $curl")
   
    val response = request.send(backend)
    response map (r => r.body match
      case Left(error) => Left(error)
      case Right(messages) => 
         messages.map(msg => 
          msg.envelope.dataMessage.map(dm => Right(SignalSimpleMessage(msg.envelope.sourceNumber,msg.envelope.sourceName, dm.message)))).flatten.sequence)


       