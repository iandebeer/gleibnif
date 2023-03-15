package dev.mn8.gleipnif

import cats.effect.*
import cats.effect.std.Dispatcher
import fs2.*
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


case class AppConf( 
  redisUrl: URL,
  redisTimeout: Int,
  ipfsClusterUrl: URL,
  universalResolverUrl: URL,
  universalResolverTimeout: Int,
  ipfsClusterTimeout: Int,
  pollingInterval: Int,
  passkitHost: URL,
  passkitPort: Int,
  passkitApiPrefix: String,
  passkitRestKey: String,
  passkitSecret: String
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
    |passkitHost: $passkitHost
    |passkitPort: $passkitPort
    |passkitApiPrefix: $passkitApiPrefix
    |passkitRestKey: $passkitRestKey
    |passkitSecret: $passkitSecret
    |""".stripMargin

object Main extends IOApp:
  val pollingInterval: FiniteDuration = 30.seconds
  val signalBot = SignalBot()
  val openAIAgent = OpenAIAgent()
  import PasskitAgent.*

  def getConf() = 
    val appConf: AppConf = ConfigSource.default.at("openai-conf").load[AppConf]  match
      case Left(error) => 
        println(s"Error: $error")
        AppConf(new URL(""), 0, new URL(""), new URL(""), 0, 0, 0, new URL(""), 0, "", "", "")
      case Right(conf) => conf  
    appConf

  val appConf= getConf() 
  def callEndpoint: IO[Unit] = 
    for
      messages: List[SignalSimpleMessage] <- signalBot.receive()
      addMessages: List[SignalSimpleMessage] <- IO.delay(messages.filter(m => m.text.toLowerCase().startsWith("@admin|add") ))
      a: List[PasskitAgent] <- IO.delay(addMessages.map(m => PasskitAgent(m.name,"did:prism1234567","https://google.com")))
      p: List[Array[Byte]] <- a.map(s => s.signPass()).sequence  
      z <- p.map(e => base64Encode(e)).sequence
 
      convoMessages <- IO.delay(messages.filter(m => !m.text.toLowerCase().startsWith("@admin") ))
      keywords <- convoMessages.map(m => openAIAgent.extractKeywords(m)).sequence
      //_ <- IO.println(s"Received keywords: $keywords")
      _ <- addMessages.map(m => 
        signalBot.send(SignalSendMessage(List[String](),s"${m.name}, Welcome to D@wnPatrol\nAttached is a DID-card you can add to your Apple wallet","+27659747833",List(m.phone)))).sequence  
      _ <- keywords.map(k => 
          signalBot.send(SignalSendMessage(List[String](),s"${k.name}, I have extracted the following keywords: ${k.keywords.mkString(",")}","+27659747833",List(k.phone)))
        ).sequence
    yield ()
      
  val stream: Stream[IO, Unit] =
    Stream
    .repeatEval(callEndpoint)
    .metered(pollingInterval)

  override def run(args: List[String]): IO[ExitCode] = 
   stream.evalTap(response => IO(print(s"*")))
   .compile
   .drain.as(ExitCode.Success)
