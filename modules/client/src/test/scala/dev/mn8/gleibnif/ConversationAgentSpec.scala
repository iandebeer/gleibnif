package dev.mn8.gleibnif

import cats.effect.IO
import cats.effect.kernel.Resource
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.reasoning.pdf.*
import com.xebia.functional.xef.scala.conversation.*
import dev.mn8.gleibnif.connection.RedisStorage
import dev.mn8.gleibnif.didcomm.DID
import dev.mn8.gleibnif.didcomm.DIDTypes.*
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.RedisCommands
import io.circe.Decoder
import io.circe.syntax._
import munit.CatsEffectSuite

import scala.concurrent.duration.Duration

class ConversationAgentSpec extends CatsEffectSuite {
  override val munitTimeout = Duration(60 * 3, "s")
  val did: DIDUrl = DID(
    createMethodName("example").get,
    createMethodSpecificId("123456789abcdefghi").get
  ).toDIDUrl
  private final case class AIResponse(answer: String) derives SerialDescriptor, Decoder

  test("writeToRedis should store words as JSON") {
    val aspect1 = "Location"
    val aspect2 = "Time"
    val words1  = List("New York", "Los Angeles")
    val words2  = List("today", "next week")

    RedisStorage.create(s"redis://localhost:6379").use { storage =>
      for {
        _ <- storage.writeToRedis(did, aspect1, words1)
        _ <- storage.writeToRedis(did, aspect2, words2)

        result1 <- storage.readFromRedis(did, aspect1)
        result2 <- storage.readAllFromRedis(did)

        _ <- IO { println(s"result = $result2") }
      } yield assertEquals(result1, Some(words1.asJson.noSpaces))
    }
  }
  test("reverse lookup of DID by phone number") {
    val phoneNumber = "+1 212 555 1212"

    RedisStorage.create(s"redis://localhost:6379").use { storage =>
      for {
        _ <- storage.storePhoneNumber(did, phoneNumber)
        d <- storage.getDidByPhoneNumber(phoneNumber)
        p <- storage.getPhoneNumber(d.get)
        _ <- IO.println(s"result = $d <-> $p")
      } yield assertEquals((p, d), (Some(phoneNumber), Some(did)))
    }
  }
  test("reverse lookup of DID by email") {
    val email = "joe@domain.com"
    RedisStorage.create(s"redis://localhost:6379").use { storage =>
      for {
        _ <- storage.storeEmail(did, email)
        d <- storage.getDidByEmail(email)
        e <- storage.getEmail(did)
        _ <- IO { println(s"result = $d <-> $e") }
      } yield assertEquals((e, d), (Some(email), Some(did)))
    }
  }
  test("pdf reasoning") {
    println(s"open api key: ${sys.env.get("OPENAI_TOKEN")}")

    val pdfUrl =
      "https://www.commercebank.com/-/media/cb/pdf/personal/bank/statement_sample1.pdf"
    conversation {
      val pdf = PDF(
        OpenAI.FromEnvironment.DEFAULT_CHAT,
        OpenAI.FromEnvironment.DEFAULT_SERIALIZATION,
        summon[ScalaConversation]
      )
      addContext(Array(pdf.readPDFFromUrl.readPDFFromUrl(pdfUrl).get()))
      while (true) {
        println("Enter your question: ")
        val line     = scala.io.StdIn.readLine()
        val response = prompt[AIResponse](Prompt(line))
        println(s"${response.answer}\n---\n")
      }
    }
  }

}
