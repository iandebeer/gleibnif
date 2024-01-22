package dev.mn8.gleibnif.dawn

import cats.*
import cats.effect._
import cats.implicits._
import cats.implicits.catsSyntaxApplicativeId
import cats.syntax.all.*
import com.xebia.functional.xef.scala.conversation.*
import dev.mn8.gleibnif.connection.RedisStorage
import dev.mn8.gleibnif.didcomm.DID
import dev.mn8.gleibnif.didcomm.DIDTypes.*
import dev.mn8.gleibnif.openai.OpenAIAgent
import dev.mn8.gleibnif.signal.SignalBot
import dev.mn8.gleibnif.signal.*
import dev.mn8.gleibnif.signal.messages.SignalSimpleMessage
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.effect.Log.Stdout._
import io.circe.Decoder.Result
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.client3.SttpBackend

import java.time.*
import scala.deriving.*

object Aspects {
  opaque type Location  = String
  opaque type TimeFrame = String
  opaque type Action    = String
  opaque type Entity    = String

  object Location {
    def apply(value: String): Location = value
  }

  object TimeFrame {
    def apply(value: String): TimeFrame = value
  }

  object Action {
    def apply(value: String): Action = value
  }

  object Entity {
    def apply(value: String): Entity = value
  }
}

case class TimeBox(timeLapse: Long, lastPing: Instant = Instant.now):
  def isExpired: Boolean =
    val now = Instant.now
    Duration.between(lastPing, now).toSeconds > timeLapse
  def ping: TimeBox =
    val now = Instant.now
    TimeBox(timeLapse, now)

case class LocationConversation(
    place: String,
    did: DID
): // derives SerialDescriptor:
  enum LocationRequest:
    def question: String =
      this match
        case Home =>
          "Where is your home? Please drop a pin when you are at home"
        case Work =>
          "Where is your work? Please drop a pin when you are at work"
        case Delivery =>
          "Where do you want your delivery - Please drop a pin and indicatehome, work or other?"
        case Other => "What is your current location?"
    case Home extends LocationRequest
    case Work
    case Delivery
    case Other

    val s = Home.question
    def reply: String =
      this match
        case Home     => "Home"
        case Work     => "Work"
        case Delivery => "Delivery"
        case Other    => "Other"
    def isResolved: Boolean =
      this match
        case Home     => true
        case Work     => true
        case Delivery => true
        case Other    => true

case class InviteConversation():
  enum InviteRequest:
    def question: String =
      this match
        case Email => "What is your email address?"
        case Other => "What is your email address?"
    case Email extends InviteRequest
    case Other

    def reply: String =
      this match
        case Email => "Email"
        case Other => "Other"
    def isResolved: Boolean =
      this match
        case Email => true
        case Other => true
    def extractEmail(text: String): Option[String] =
      val emailRegex =
        "\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}\\b".r
      emailRegex.findFirstIn(text)

object ConversationCodec:
  given encodeLocationConversation: Encoder[LocationConversation] =
    new Encoder[LocationConversation]:
      final def apply(c: LocationConversation): Json =
        Json.obj(
          ("did", Json.fromString(c.did.toString))
        )
  given decodeLocationConversation: Decoder[LocationConversation] =
    new Decoder[LocationConversation]:
      final def apply(c: HCursor): Result[LocationConversation] =
        for {
          did <- c.downField("did").as[String]
        } yield LocationConversation(
          "",
          fromDIDUrl(did.asInstanceOf[DIDUrl]).get
        )

final case class ConversationAgent(signalPhone: String)(using
    redis: RedisCommands[cats.effect.IO, String, String],
    logger: Logger[cats.effect.IO],
    backend: SttpBackend[cats.effect.IO, Any]
):

  import Aspects._
  // given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val redisStorage: Resource[cats.effect.IO, RedisStorage] =
    RedisStorage.create("localhost:6379")
  val signalBot   = SignalBot(backend)
  val openAIAgent = OpenAIAgent(backend)

  def handleConversation(m: SignalSimpleMessage) = ???
  val timeBox: TimeBox                           = TimeBox(5, Instant.now)
