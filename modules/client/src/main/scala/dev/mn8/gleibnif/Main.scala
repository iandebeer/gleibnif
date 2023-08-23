package dev.mn8.gleipnif

import cats.effect.*
import cats.effect.std.Dispatcher
import cats.Monad
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.data.{EitherT, WriterT}
import cats.implicits._
import cats.syntax.all._
import cats.FunctorFilter.ops.toAllFunctorFilterOps
import cats.syntax.traverse._
import fs2.Compiler.Target.forSync
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import Constants.*

import javax.security.auth.login.AppConfigurationEntry
import java.net.URL
import java.util.concurrent.Executor

import io.circe.parser.*
import io.circe._, io.circe.parser._, io.circe.syntax._
import fs2.Stream

import dev.mn8.gleibnif.signal.*
import SignalMessageCodec.memberDecoder
import dev.mn8.gleibnif.didops.RegistryServiceClient
import dev.mn8.gleibnif.dawn.DWNClient
import dev.mn8.gleibnif.passkit.PasskitAgent
import dev.mn8.gleibnif.openai.OpenAIAgent
import sttp.client3.ResponseException
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import dev.mn8.gleibnif.didcomm.DIDDoc
import dev.mn8.gleibnif.didcomm.Service
import java.net.URI
import dev.mn8.gleibnif.didops.RegistryRequest
import dev.mn8.gleibnif.didops.RegistryResponseCodec.encodeRegistryRequest
import dev.mn8.gleibnif.didcomm.ServiceEndpointNodes
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.client3.SttpBackend

import dev.mn8.castanet.*
import scala.collection.immutable.ListSet
import cats.data.IndexedStateT
import dev.mn8.gleibnif.connection.RedisStorage
import dev.mn8.gleibnif.connection.RedisStorage.*

import dev.mn8.gleibnif.dawn.ConversationAgent
import dev.mn8.gleibnif.didcomm.DID
import dev.mn8.gleibnif.dawn.Aspects.Action
import dev.mn8.gleibnif.config.ConfigReaders.*
import dev.mn8.gleibnif.logging.LogWriter.{err, info, logNonEmptyList}
import dev.mn8.gleibnif.ConversationPollingHandler

object Main extends IOApp.Simple:
  // override protected def blockedThreadDetectionEnabled = true
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  val pollingInterval: FiniteDuration = 10.seconds
  val pollingHandler = new ConversationPollingHandler(using logger)
  val run = Dispatcher[IO].use { dispatcher =>
    val pollingStream = for {
      backend: SttpBackend[cats.effect.IO, Any] <- Stream.resource(
        AsyncHttpClientCatsBackend.resource[IO]()
      )
      _ <- Stream.fixedRate[IO](10.seconds) // Poll every 10 seconds
      data <- Stream.eval(pollingHandler.converse(backend))
    } yield data

    val pollingWithLogging = pollingStream.compile.drain
    for {
      fiber <- pollingWithLogging.start
      _ <- fiber.join
    } yield ()
  }
