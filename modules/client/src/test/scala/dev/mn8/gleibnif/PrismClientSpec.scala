package dev.mn8.gleibnif

import dev.mn8.gleibnif.prism.CreateDIDResponse
import dev.mn8.gleibnif.prism.PrismClient
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import munit.*
import sttp.client3.*
import sttp.client3.circe.*

import java.net.URI
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class PrismClientSpec extends FunSuite {
  val baseURL = "http://13.244.55.248:8080/prism-agent"
  val apiKey = "kxr9i@6XgKBUxe%O"

  test("PrismClient should be able to create a DID") {
    val client = PrismClient(baseURL, apiKey)
    client.createUnpublishedDID() match {
      case Left(error)  => throw new Exception(s"Error creating DID: $error")
      case Right(value) => println(s"DID created successfully: $value")
    }
  }

  test("PrismClient should be able to publish a DID") {
    val testDID =
      "did:prism:28d8341f62b29054736450f8bc9cb8117792b87e4763f0a2fe4c1c5d18dd358f"

    val client = PrismClient(baseURL, apiKey)
    client.publishDID(testDID) match {
      case Left(error)  => throw new Exception(s"Error publishing DID: $error")
      case Right(value) => println(s"DID published successfully: $value")
    }
  }

  test(
    "PrismClient should be able to get the response from publishing a DID, or return an example DID"
  ) {
    val client = PrismClient(baseURL, apiKey)

    client.createUnpublishedDID() match {
      case Left(error) => throw new Exception(s"Error creating DID: $error")
      case Right(jsonResponse) =>
        println(s"DID created successfully: $jsonResponse")

        val parsedResponse =
          decode[CreateDIDResponse](jsonResponse).toOption

        val longFormDID = parsedResponse.map(_.longFormDid)
        longFormDID match
          case None =>
            throw new Exception(s"Error parsing response for creating DID")
          case Some(value) => client.getPublishDIDResponse(value)
    }
  }
}
