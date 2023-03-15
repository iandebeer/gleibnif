package dev.mn8.gleibnif.signal


import SignalMessageCodec.*

import SignalMessageCodec.signalMessageDecoder
import SignalMessageCodec.signalSendMessage
import io.circe.*
import io.circe.syntax.*
import sttp.client3.*
import sttp.model.*
import sttp.client3.circe.*
import cats.implicits._

import io.circe.parser.*
import cats.effect.IO
import pureconfig.*
import pureconfig.generic.derivation.default.*

case class SignalConfig(
  signalUrl: String,
  signalUser: String,
  signalPhone: String,
  signalTimeout: Int = 5
) derives ConfigReader:
  override def toString: String = s"SignalConfig(url: ${signalUrl.toString}, user: ${signalUser.toString}, phone: ${signalPhone.toString})"

case class SignalBot():
  val backend = HttpClientSyncBackend()
  def getConf() = 
    val signalConf: SignalConfig = ConfigSource.default.at("signal-conf").load[SignalConfig]  match
      case Left(error) => 
        println(s"Error: $error")
        SignalConfig("", "", "" )
      case Right(conf) => conf
    signalConf

  val signalConf= getConf() 

  def init(): Unit = ()
  def register(voiceMode:Boolean) = 
    val request = basicRequest.contentType("application/json").body(s"""{"use_voice": $voiceMode}""").post(
      uri"${signalConf.signalUrl}/v1/register/${signalConf.signalPhone}")
    val response = request.send(backend) 
    response.code.toString match
      case "200" => 
        println("Registration successful")
        true
      case "409" => 
        println("Registration failed")
        false
      case _ => 
        println("Registration failed")
        false

    

    
  def verify(pin:String) = 
    val request = basicRequest.contentType("application/json").body(s"""{"pin": $pin""").post(
      uri"${signalConf.signalUrl}/v1/verify/${signalConf.signalPhone}")
    val response = request.send(backend) 
    response.code.toString match
      case "200" => 
        println("Verification successful")
        true
      case "409" => 
        println("Verification failed")
        false
      case _ => 
        println("Verification failed")
        false

  def send(message: SignalSendMessage): IO[Unit] = 
    for 
      request <- IO.blocking(basicRequest.contentType("application/json").body(message.asJson.noSpaces).post(
         uri"${signalConf.signalUrl}/v2/send"))
      _ <- IO(println(s"Sending message: $message"))
      _ <- IO(println(s"Sending message: ${message.asJson.noSpaces}"))
      _ <- IO.blocking(request.send(backend).body match
        case Left(error) => 
          IO.println(error)
        case Right(response) => 
          IO.println(response))
    yield ()

  def receive(): IO[List[SignalSimpleMessage]] = 
    for 
      request <- IO.blocking(basicRequest.contentType("application/json")get(
        uri"${signalConf.signalUrl}/v1/receive/${signalConf.signalPhone}?timeout=${signalConf.signalTimeout}"))
      _ <- IO(println("Receiving messages"))
      response <-  IO.blocking(request.send(backend))
      _ <- IO.println(response.body)
      messages <-  IO(response.body match
        case Left(error) => 
          IO.println(error)
          List[SignalMessage]()
        case Right(messages) => 
          //println(messages)
          parse(messages) match
            case Left(error) => 
              IO.println(error)
              List[SignalMessage]()
            case Right(json) => 
              //println(json)
              json.as[List[SignalMessage]] match
                case Left(error) => 
                  println(error)
                  List[SignalMessage]()
                case Right(messages) => 
                  IO.println(messages.mkString("\n"))
                  messages)

      result <- IO(messages.flatMap(m =>
        List(
          m.envelope.dataMessage.map(dm => SignalSimpleMessage(m.envelope.sourceNumber, m.envelope.sourceName,dm.message)),
         // m.envelope.syncMessage.map(sm => SignalSimpleMessage(m.envelope.sourceNumber, m.envelope.sourceName,sm.sentMessage.map(snt => snt.message).getOrElse(""))),
         // m.envelope.sentMessage.map(sm => SignalSimpleMessage(m.envelope.sourceNumber, m.envelope.sourceName,sm.message))
        )
      ).flattenOption)
    yield result

    

       