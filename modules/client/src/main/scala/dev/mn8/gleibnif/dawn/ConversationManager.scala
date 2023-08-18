package dev.mn8.gleibnif.dawn

import scala.concurrent.duration.FiniteDuration
import cats.effect.*
import dev.profunktor.redis4cats.RedisCommands
import dev.mn8.gleibnif.didcomm.* 
import dev.mn8.gleibnif.didcomm.DIDTypes.*
import dev.mn8.gleibnif.connection.RedisStorage
import cats.effect.kernel.Resource
import dev.profunktor.redis4cats.Redis

object ConversationManager extends IOApp.Simple:
  val redis: Resource[cats.effect.IO, RedisStorage] = RedisStorage.create("redis://localhost:6379")
  val run = redis.use { r =>
    ConversationManager(r).run
  }

  

 
class ConversationManager(redis: RedisStorage):

  def run: IO[Unit] = 
    for
      _ <- IO.println("Starting Conversation Manager")

    yield ()

  private def getDID(phone:Option[String], email:Option[String]): IO[Option[DID]] =
    (phone, email) match
        case (Some(phoneNumber), _) => redis.getDidByPhoneNumber(s"$phoneNumber").flatMap(u => u match
          case Some(url) => IO.pure(fromDIDUrl(url))
          case None => IO.pure(None))
         
        case (_, Some(emailAddress)) => redis.getDidByEmail(s"$emailAddress").flatMap(u => u match
          case Some(url) => IO.pure(fromDIDUrl(url))
          case None => IO.pure(None))
        case _ =>  IO.pure(None)
  
 /*  def getOrCreateAgent(clientId: String, ttl: FiniteDuration): IO[ConversationAgent] =
    redis.getAgent(clientId).flatMap {
      case Some(_) =>
        // Reset the TTL since there was an interaction
        redis.expire(clientId, ttl) *> IO.pure(ConversationAgent(clientId))
      case None =>
        // Create a new agent and set with a TTL
        redis.setEx(clientId, Agent(clientId).asJson.noSpaces, ttl) *> IO.pure(ConversationAgent(clientId))
    }   
 */
