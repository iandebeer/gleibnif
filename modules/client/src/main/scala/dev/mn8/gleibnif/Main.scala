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


object Main extends IOApp:
  val pollingInterval: FiniteDuration = 15.seconds
  val signalBot = SignalBot()
  val openAIAgent = OpenAIAgent()
  def callEndpoint: IO[Unit] = 
    for
      messages <- signalBot.receive()
      _ <- IO(println(s"Received messages: $messages"))
      keywords <- messages.map(m => openAIAgent.extractKeywords(m)).sequence
      _ <- IO(println(s"Received keywords: $keywords"))
      _ <- keywords.map(k => 
          signalBot.send(SignalSendMessage(s"${k.name}, I have extracted the following keywords: ${k.keywords.mkString(",")}","27659747833",List(k.phone)))
        ).sequence
    yield ()
      
  val stream: Stream[IO, Unit] =
    Stream
    .repeatEval(callEndpoint)
    .metered(pollingInterval)

  override def run(args: List[String]): IO[ExitCode] = 
   stream.evalTap(response => IO(println(s"Received response: $response")))
   .compile
   .drain.as(ExitCode.Success)
