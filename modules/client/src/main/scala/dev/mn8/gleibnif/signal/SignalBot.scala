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
    println(s"Signal Conf: $signalConf")
    println(s"Phone: ${signalConf.signalPhone}")  
    signalConf

  val signalConf= getConf() 

  def init(): Unit = ()
  def register(voiceMode:Boolean): IO[Either[Exception,String]] = 
    val request = basicRequest.contentType("application/json").body(s"""{"use_voice": $voiceMode}""").post(
      uri"${signalConf.signalUrl}/register/${signalConf.signalPhone}")
    val response = request.send(backend) 
    response.code.toString match
      case "200" => 
        IO(Right("200: Message sent"))
      case "409" =>
        IO(Left(new Exception("409: Message failed")))
      case _ =>
        IO(Left(new Exception("Message failed")))
    
  def verify(pin:String) : IO[Either[Exception,String]] = 
    val request = basicRequest.contentType("application/json").body(s"""{"pin": $pin""").post(
      uri"${signalConf.signalUrl}/verify/${signalConf.signalPhone}")
    val response = request.send(backend) 
    response.code.toString match
      case "200" => 
        IO(Right("200: Message sent"))
      case "409" =>
        IO(Left(new Exception("409: Message failed")))
      case _ =>
        IO(Left(new Exception("Message failed")))

  def send(message: SignalSendMessage): EitherT[IO,Exception,String] =
    val request = basicRequest.contentType("application/json").body(message.asJson.noSpaces).post(
      uri"${signalConf.signalUrl}/v2/send")
    val curl = request.toCurl
    request.headers.foreach(println)
    println(s"curl: \n $curl")
    val response = request.send(backend)
    println(s"Response: ${response.body}")
    response.code.toString match
      case "200" => 
        EitherT(IO(Right("200: Message sent")))
      case "409" =>
        EitherT(IO(Left(new Exception("409: Message failed"))))
      case _ =>
        EitherT(IO(Left(new Exception("Message failed"))))

  def receive(): EitherT[IO, Exception,List[SignalSimpleMessage]] = 
    val request = basicRequest.contentType("application/json").
      response(asJson[List[SignalMessage]]).
      get(uri"${signalConf.signalUrl}/v1/receive/${signalConf.signalPhone}?timeout=${signalConf.signalTimeout}")
    val messages: EitherT[IO, ResponseException[String, Error], List[SignalMessage]] = EitherT(IO(request.send(backend).body))
    val curl = request.toCurl
    request.headers.foreach(println)
    println(s"curl: \n $curl")
    for
      _ <- EitherT.right(IO.println("Received messages"))
      ms <- messages
      d <- EitherT.right(IO(ms.map(m => m.envelope.dataMessage.map(dm => SignalSimpleMessage(m.envelope.sourceNumber, m.envelope.sourceName,dm.message))).flattenOption))
    yield d

    

       