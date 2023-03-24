package dev.mn8.gleibnif



import munit.FunSuite
import dev.mn8.gleibnif.signal.*
import dev.mn8.gleibnif.openai.OpenAIAgent
import cats.effect.IO
import cats.effect.unsafe.implicits._
import dev.mn8.gleibnif.signal.SignalSimpleMessage
import io.circe._, io.circe.parser._, io.circe.syntax._

import dev.mn8.gleibnif.signal.SignalMessageCodec.memberDecoder
import dev.mn8.gleibnif.didops.RegistryServiceClient


class RegistryClientSpec extends FunSuite {
  val baseURL = "https://api.godiddy.com/0.1.0/universal-registrar/"
  val apiKey = "c2850992-32fd-4ccf-9352-77aa329eef13"
  val document = """{
  "didDocument": {
    "@context": [
      "https//www.w3.org/ns/did/v1"
    ],
    "service": [
      {
        "id": "#dwn",
        "type": "DecentralizedWebNode",
        "serviceEndpoint": {
          "nodes": [
            "https://dwn.example.com",
            "https://example.org/dwn"
          ]
        }
      }
    ],
    "verificationMethod": [

    ]
  },
  "options": {
    "network": "danube"
  },
  "secret": {
  }
}""".stripMargin
  val client = RegistryServiceClient(baseURL, apiKey, document)

  test("RegistryClient should be able to create a DID") {
    val r = for 
      did <- client.createDID("indy")
    yield
      assert(did.startsWith("did:"))
    r.flatTap(m => IO(println(s"$r"))).unsafeRunSync()

  }


}
