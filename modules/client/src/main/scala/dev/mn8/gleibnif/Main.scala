package dev.mn8.gleipnif

import cats.effect.*
import cats.effect.std.Dispatcher
import cats.Monad
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.data.EitherT
import cats.implicits._
import cats.syntax.all.toTraverseOps
import cats.FunctorFilter.ops.toAllFunctorFilterOps

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import Constants.*

import javax.security.auth.login.AppConfigurationEntry
import java.net.URL
import java.util.concurrent.Executor

import pureconfig.*
import pureconfig.generic.derivation.default.*

import io.circe.parser.*
import fs2.Stream

import dev.mn8.gleibnif.signal.*
import SignalMessageCodec.memberDecoder
import dev.mn8.gleibnif.didops.RegistryServiceClient
import dev.mn8.gleibnif.dawn.DWNClient
import dev.mn8.gleibnif.passkit.PasskitAgent
import dev.mn8.gleibnif.openai.OpenAIAgent
import sttp.client3.ResponseException
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

object Main extends IOApp:
  type ErrorOr[A] = EitherT[IO, Exception, A]

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

  case class RegistryConf(
      registrarUrl: URL,
      registrarTimeout: Int,
      apiKey: String
  ) derives ConfigReader:
    override def toString(): String =
      s"""
      |registryUrl: $registrarUrl
      |registryTimeout: $registrarTimeout
      |""".stripMargin

  def getConf() =
    val appConf: AppConf =
      ConfigSource.default.at("app-conf").load[AppConf] match
        case Left(error) =>
          println(s"Error: $error")
          AppConf(
            new URL("http://localhost:8080"),
            0,
            new URL("http://localhost:8080"),
            new URL("http://localhost:8080"),
            0,
            0,
            0,
            new URL("http://localhost:8080"),
            "",
            new URL("http://localhost:8080")
          )

        case Right(conf) => conf
    appConf
  def getRegistryConf() =
    val registryConf: RegistryConf =
      ConfigSource.default.at("registry-conf").load[RegistryConf] match
        case Left(error) =>
          println(s"Error: $error")
          RegistryConf(new URL("http://localhost:8080"), 0, "")
        case Right(conf) => conf
    registryConf

  val registryConf = getRegistryConf()
  val appConf = getConf()
  val signalBot = SignalBot()
  val openAIAgent = OpenAIAgent()
  val registryClient = RegistryServiceClient(
    registryConf.registrarUrl.toString(),
    registryConf.apiKey
  )
  

  def callServices(): IO[Either[Exception, List[String]]] =
    val messages: EitherT[IO, ResponseException[String, io.circe.Error], List[SignalSimpleMessage]] = EitherT(AsyncHttpClientCatsBackend.resource[IO]().use { b => 
       signalBot.receive(b)})
    (for
      mt <- messages.map(_.partition(m => m.text.toLowerCase().startsWith("@admin|add")))
      adminMsg <- mt._1.map(m =>
        for
          // add content to the didDocument
          did <- registryClient.createDID("indy", "")
          member <- EitherT(IO.delay(decode[Member](m.text.split("\\|")(2))))
          pass <- PasskitAgent(member.name, did, appConf.dawnUrl).signPass()
          r <- signalBot.send(
            SignalSendMessage(
              List[String](
                s"data:application/vnd.apple.pkpass;filename=did.pkpass;base64,${pass}"
              ),
              s"${member.name}, Welcome to D@wnPatrol\nAttached is a DID-card you can add to your Apple wallet. \nIf you have an Android phone consider using https://play.google.com/store/apps/details?id=color.dev.com.tangerine to import into your preferred wallet \nFeel free to use me as your personal assistant ;)",
              "+27659747833",
              List[String](member.number))
          )
        yield r).sequence

      keywords <- mt._2.map(m =>
        openAIAgent.extractKeywords(m)
      ).sequence
      s <-  keywords.map(k => signalBot.send(
            SignalSendMessage(
              List[String](),
              s"${k.name}, I have extracted the following keywords: ${k.keywords
                  .mkString(",")}",
              "+27659747833",
              List(k.phone)
            )
          )).sequence
    yield s).value  

  val pollingInterval: FiniteDuration = 30.seconds

 
  val stream: Stream[IO, Either[Exception,List[String]]] =
    Stream
      .repeatEval(callServices())
      .metered(pollingInterval)

  override def run(args: List[String]): IO[ExitCode] =
    stream
      .evalTap(response => IO(println(response)))
      .compile
      .drain
      .as(ExitCode.Success)
