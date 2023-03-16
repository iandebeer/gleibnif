package dev.mn8.gleipnif

import cats.effect.*
import cats.effect.std.Dispatcher
import java.util.concurrent.Executor
import Constants.*

import scala.concurrent.ExecutionContext.Implicits.global
import fs2.grpc.client.ClientOptions

import cats.effect.IO
import scala.concurrent.duration.*
import dev.mn8.gleibnif.signal.SignalBot
import dotty.tools.dotc.ast.untpd.Mod.Open
import dev.mn8.gleibnif.openai.OpenAIAgent
import cats.syntax.all.toTraverseOps
import dev.mn8.gleibnif.signal.SignalSendMessage
import javax.security.auth.login.AppConfigurationEntry
import java.net.URL
import pureconfig.*
import pureconfig.generic.derivation.default.*
import dev.mn8.gleibnif.passkit.PasskitAgent
import dev.mn8.gleibnif.signal.SignalSimpleMessage
import PasskitAgent.*
import io.circe.parser.*
import fs2.Stream
import dev.mn8.gleibnif.signal.Member
import dev.mn8.gleibnif.signal.SignalMessageCodec.memberDecoder
import dev.mn8.gleibnif.prism.PrismClient





case class AppConf( 
  redisUrl: URL,
  redisTimeout: Int,
  ipfsClusterUrl: URL,
  universalResolverUrl: URL,
  universalResolverTimeout: Int,
  ipfsClusterTimeout: Int,
  pollingInterval: Int,
  prismUrl: URL,
  prismToken: String,
  dawnUrl: URL
) derives ConfigReader:
  override def toString(): String = 
    s"""
    |redisUrl: $redisUrl
    |redisTimeout: $redisTimeout
    |ipfsClusterUrl: $ipfsClusterUrl
    |universalResolverUrl: $universalResolverUrl
    |universalResolverTimeout: $universalResolverTimeout
    |ipfsClusterTimeout: $ipfsClusterTimeout
    |pollingInterval: $pollingInterval
    |prismUrl: $prismUrl
    |prismToken: $prismToken
    |dawnUrl: $dawnUrl
    |""".stripMargin

object Main extends IOApp:
  val pollingInterval: FiniteDuration = 30.seconds
  val signalBot = SignalBot()
  val openAIAgent = OpenAIAgent()


  def getConf() = 
    val appConf: AppConf = ConfigSource.default.at("app-conf").load[AppConf]  match
      case Left(error) => 
        println(s"Error: $error")
        AppConf(new URL("http://localhost:8080"), 0, new URL("http://localhost:8080"), new URL("http://localhost:8080"), 0, 0, 0, new URL("http://localhost:8080"), "", new URL("http://localhost:8080"))

      case Right(conf) => conf  
    appConf

  val appConf= getConf() 
  def callEndpoints: IO[Unit] = 
    for
      messages: List[SignalSimpleMessage] <- signalBot.receive()
      addMessages: List[SignalSimpleMessage] <- IO.delay(messages.filter(m => m.text.toLowerCase().startsWith("@admin|add") ))
      
      passes <- addMessages.map(m => 
        for 
          did <- PrismClient(appConf.prismUrl.toString(),appConf.prismToken).createDID()
          member <- IO.delay(parse(m.text.split("\\|")(2)) match
            case Left(error) => 
              Member("","")
            case Right(m) => m.as[Member] match
              case Left(error) => 
                Member("","")
              case Right(member) => 
                IO.println(s"Member: $member")
                member
          )
        yield
          PasskitAgent(member.name,did,appConf.dawnUrl).signPass()).sequence
  
          
      encPasses <- passes.map(ba => 
        for 
          v <- ba
          w <- base64Encode(v)
        yield w).sequence

      _ <- addMessages.zip(encPasses).map(m => 
        signalBot.send(SignalSendMessage(List[String](
          s"data:application/vnd.apple.pkpass;filename=did.pkpass;base64,${m._2}"),
          s"${m._1.name}, Welcome to D@wnPatrol\nAttached is a DID-card you can add to your Apple wallet. \nIf you have an Android phone consider using https://play.google.com/store/apps/details?id=color.dev.com.tangerine to import into your preferred wallet \nFeel free to use me as your personal assistant ;)",
          "+27659747833",
          List[String](m._1.phone)))).sequence
 
      convoMessages <- IO.delay(messages.filter(m => !m.text.toLowerCase().startsWith("@admin") ))
      keywords <- convoMessages.map(m => openAIAgent.extractKeywords(m)).sequence   
      _ <- keywords.map(k => 
          signalBot.send(SignalSendMessage(List[String](),s"${k.name}, I have extracted the following keywords: ${k.keywords.mkString(",")}","+27659747833",List(k.phone)))
        ).sequence
    yield ()
      
  val stream: Stream[IO, Unit] =
    Stream
    .repeatEval(callEndpoints)
    .metered(pollingInterval)

  override def run(args: List[String]): IO[ExitCode] = 
   stream.evalTap(response => IO(print(s"*")))
   .compile
   .drain.as(ExitCode.Success)
