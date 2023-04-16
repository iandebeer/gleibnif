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

case class SignalBot():
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  type ErrorOr[A] = EitherT[IO, Exception, A]
  val backend = HttpClientSyncBackend()
  def log[T](value: T)(implicit logger: Logger[IO]): IO[Unit] =
    logger.info(s"SignalBot: $value")
  def getConf() = 
    val signalConf: SignalConfig = ConfigSource.default.at("signal-conf").load[SignalConfig]  match
      case Left(error) => 
        println(s"Error: $error")
        SignalConfig("", "", "" )
      case Right(conf) => conf
    log(s"Signal Conf: $signalConf")
    log(s"Phone: ${signalConf.signalPhone}")  
    signalConf

  val signalConf= getConf() 
  


  def init(): Unit = ()
  def register(voiceMode:Boolean): ErrorOr[String] = 
    log("Registering...")
    val request = basicRequest.contentType("application/json").body(s"""{"use_voice": $voiceMode}""").post(
      uri"${signalConf.signalUrl}/register/${signalConf.signalPhone}")
    val response = request.send(backend) 
    log("Registered")

    response.code.toString match
      case "200" => 
        EitherT(IO(Right("Signalbot Register: 200 - Message sent")))
      case "409" =>
        EitherT(IO(Left(new Exception("Signalbot Register: 409 - Message failed"))))
      case _ =>
        EitherT(IO(Left(new Exception("Signalbot Register: Message failed"))))

    
  def verify(pin:String) :  ErrorOr[String] = 
    log("Verifying...")
    val request = basicRequest.contentType("application/json").body(s"""{"pin": $pin""").post(
      uri"${signalConf.signalUrl}/verify/${signalConf.signalPhone}")
    val response = request.send(backend) 
    log("Verified")
    response.code.toString match
      case "200" => 
        EitherT(IO(Right("Signalbot Verify: 200 - Message sent")))
      case "409" =>
        EitherT(IO(Left(new Exception("Signalbot Verify: 409 - Message failed"))))
      case _ =>
        EitherT(IO(Left(new Exception("Signalbot Verify: Message failed"))))


  def send(message: SignalSendMessage, backendA: SttpBackend[IO, Any]): IO[Either[Exception, String]] =
    log(s"Sending message: $message")
    val request = basicRequest.contentType("application/json").body(message.asJson.noSpaces).post(
      uri"${signalConf.signalUrl}/v2/send")
    // val curl = request.toCurl
    // request.headers.foreach(println)
    // println(s"curl: \n $curl")
    val response = request.send(backendA)
    // println(s"Response: ${response.body}")
    log(s"Sent message: $message")
    response.map(c => c.code match
      case s:StatusCode if s.isSuccess => 
        Right(s"Signalbot Send: $s - Message sent")
      case s:StatusCode =>
        Left(new Exception(s"Signalbot Send: $s "))
      case _ =>
        Left(new Exception("Signalbot Send: Message failed")))


  def receive(backendA: SttpBackend[IO, Any]): IO[Either[ResponseException[String, Error], List[SignalSimpleMessage]]] = 
    log("Receiving messages...")
    val request = basicRequest.contentType("application/json").
      response(asJson[List[SignalMessage]]).
      get(uri"${signalConf.signalUrl}/v1/receive/${signalConf.signalPhone}?timeout=${signalConf.signalTimeout}")
   // val messages: EitherT[IO, ResponseException[String, Error], List[SignalMessage]] = EitherT(IO(request.send(backendA).map(b =>
    // val curl = request.toCurl
    // request.headers.foreach(println)
    // println(s"curl: \n $curl")
   
    val response = request.send(backendA).map(b =>
        b.body match
          case Left(error) => 
            log(s"Error: $error")
            Either.left(error)
          case Right(messages) => 
            log(s"Received messages... ${messages.toString}")
            Either.right(messages)
      )
    // response.flatMap(
    //   r => r match
    //     case Left(error) => IO(Left(error))
    //     case Right(messages) => IO(Right(messages))
    // ).map(e => e.map(messages => 
    //   messages.map(m =>
    //     m.envelope.dataMessage match
    //       case Some(dm) => SignalSimpleMessage(m.envelope.sourceNumber, m.envelope.sourceName,dm.message)
    //       case None =>  IO(Left("no data"))))) //SignalSimpleMessage(m.envelope.sourceNumber, m.envelope.sourceName,""))))  

      val result = for 
        r <- EitherT(response)
        messages = r.map { m =>
          
          m.envelope.dataMessage match {
            case Some(dm) => SignalSimpleMessage(m.envelope.sourceNumber, m.envelope.sourceName, dm.message)
            case None => SignalSimpleMessage(m.envelope.sourceNumber, m.envelope.sourceName, "")
          }
        }
      yield messages
      result.value

        
        //m.envelope.dataMessage.map(dm => SignalSimpleMessage(m.envelope.sourceNumber, m.envelope.sourceName,dm.message))))
    
    

       