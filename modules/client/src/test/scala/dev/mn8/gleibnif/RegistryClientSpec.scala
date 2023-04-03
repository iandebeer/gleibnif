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
import java.net.URI
import dev.mn8.gleibnif.DIDDoc
import dev.mn8.gleibnif.DIDCodec.decodeDIDDoc
import dev.mn8.gleibnif.DIDCodec.encodeDIDDoc
import dev.mn8.gleibnif.didops.RegistryRequest
import dev.mn8.gleibnif.didops.RegistryResponseCodec.encodeRegistryRequest



class RegistryClientSpec extends FunSuite {
  val baseURL = "https://api.godiddy.com/0.1.0/universal-registrar/"
  val apiKey = "c2850992-32fd-4ccf-9352-77aa329eef13"
  val document1 = """{
  "didDocument": {
    "@context": [
      "https//www.w3.org/ns/did/v1"
    ],
    "controller": "did:example:bcehfew7h32f32h7af3",

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
  val client = RegistryServiceClient(baseURL, apiKey)

  test("RegistryClient should be able to create a DID") {
    val doc = DIDDoc("",Some("did:example:123456789"),Some(Set("tel:12345k;name=Ian de Beer")),None,None,None,None,None,None,
    Some(Set(Service(id= new URI("#dwn"), `type`= Set("DecentralizedWebNode"), serviceEndpoint=Set(ServiceEndpointNodes(
    nodes=Set(new URI("https://dwn.example.com"), new URI("https://example.org/dwn"))))))))
    val reg = RegistryRequest(doc)
    val document = reg.asJson.spaces2

    
    val r = for 
      did <- client.createDID("indy",document)
      _ <- IO(println(s"did: $did"))
    yield
      assert(did.startsWith("did:"))
    r.flatTap(m => IO(println(s"$r"))).unsafeRunSync()

  }


}
