package dev.mn8.gleibnif

import cats.effect._
import dev.mn8.gleibnif.prism.CreateDIDResponse
import dev.mn8.gleibnif.prism.DocumentTemplate
import dev.mn8.gleibnif.prism.PrismClient
import dev.mn8.gleibnif.prism.PublicKey
import dev.mn8.gleibnif.prism.Service
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import munit.CatsEffectSuite
import munit.*
import sttp.client3.*
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.circe.*

import java.net.URI
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class PrismClientSpec extends CatsEffectSuite {
  val baseURL = "http://13.244.55.248:8080/prism-agent"
  val apiKey  = "kxr9i@6XgKBUxe%O"

  val backendR: Resource[IO, SttpBackend[IO, Any]] =
    AsyncHttpClientCatsBackend.resource[IO]()

  val documentTemplateSample = DocumentTemplate(
    Seq(
      PublicKey("key1", "authentication"),
      PublicKey("key2", "assertionMethod")
    ),
    Seq(
      Service("did:prism:test1", "LinkedDomains", Seq("https://test1.com")),
      Service("did:prism:test2", "LinkedDomains", Seq("https://test2.com"))
    )
  )

  test("PrismClient should be able to create a DID") {
    backendR.use { backend =>
      val client = PrismClient(baseURL, apiKey, backend)
      client
        .createUnpublishedDID(documentTemplateSample)
        .map(r =>
          r match {
            case Left(error) =>
              throw new Exception(s"Error creating DID: $error")
            case Right(value) => println(s"DID created successfully: $value")
          }
        )
    }
  }

  test("PrismClient should be able to publish a DID") {
    val testDID =
      "did:prism:cd72993bed4536330b585dde988437680ebaa01f78cabb2c17bab9f5d2e3cbcc"

    backendR.use { backend =>
      val client = PrismClient(baseURL, apiKey, backend)
      client
        .publishDID(testDID)
        .map(r =>
          r match {
            case Left(error) =>
              throw new Exception(s"Error publishing DID: $error")
            case Right(value) => println(s"DID published successfully: $value")
          }
        )
    }
  }

  test(
    "PrismClient should be able to get the response from publishing a DID, or return an example DID"
  ) {
    backendR.use { backend =>
      val client = PrismClient(baseURL, apiKey, backend)

      client
        .createDID(documentTemplateSample)
        .map(r =>
          r match
            case Left(error) =>
              throw new Exception(s"Error creating DID: $error")
            case Right(value) => println(s"DID created successfully: $value")
        )
    }
  }
}
