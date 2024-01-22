package dev.mn8.gleibnif

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.std.Dispatcher
import dev.mn8.gleibnif.ConversationPollingHandler
import fs2.Stream
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

import scala.concurrent.duration.*

object Main extends IOApp.Simple:
  // override protected def blockedThreadDetectionEnabled = true
  given logger: Logger[IO]            = Slf4jLogger.getLogger[IO]
  val pollingInterval: FiniteDuration = 10.seconds
  val pollingHandler                  = new ConversationPollingHandler(using logger)
  val run = Dispatcher[IO].use { dispatcher =>
    val pollingStream = for {
      backend: SttpBackend[cats.effect.IO, Any] <- Stream.resource(
        AsyncHttpClientCatsBackend.resource[IO]()
      )
      _    <- Stream.fixedRate[IO](10.seconds) // Poll every 10 seconds
      data <- Stream.eval(pollingHandler.converse(backend))
    } yield data

    val pollingWithLogging = pollingStream.compile.drain
    for {
      fiber <- pollingWithLogging.start
      _     <- fiber.join
    } yield ()
  }
