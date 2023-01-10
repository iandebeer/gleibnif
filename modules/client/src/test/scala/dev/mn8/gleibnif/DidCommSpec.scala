package dev.mn8.gleibnif

import foundation.identity.did.*
import munit.*

import java.net.URI
import io.circe.*
import io.circe.syntax.*

import io.circe.parser.*
import dev.mn8.gleibnif.DIDDoc
import dev.mn8.gleibnif.CirceDIDCodec.decodeDIDDoc
import dev.mn8.gleibnif.CirceDIDCodec.encodeDIDDoc



class DidCommSpec extends FunSuite {

  val did = URI.create("did:ex:1234")
  val didString: String = did.toString()
  val didDocJson = """
 {
  "@context": "https://w3id.org/did-resolution/v1",
  "didDocument": {
    "@context": [
      "https://www.w3.org/ns/did/v1",
      "https://w3id.org/security/suites/jws-2020/v1",
      "https://w3id.org/security/suites/ed25519-2020/v1"
    ],
    "id": "did:example:123456789abcdefghi",
    "controller": "did:example:bcehfew7h32f32h7af3",
    "alsoKnownAs": ["did:example:bcehfew7h32f32h7af3"],
    "verificationMethod": [
      {
        "id": "did:example:123#_Qq0UL2Fq651Q0Fjd6TvnYE-faHiOpRlPVQcY_-tA4A",
        "type": "JsonWebKey2020",
        "controller": "did:example:123",
        "publicKeyJwk": {
          "crv": "Ed25519",
          "x": "VCpo2LMLhn6iWku8MKvSLg2ZAoC-nlOyPVQaO3FxVeQ",
          "kty": "OKP",
          "kid": "_Qq0UL2Fq651Q0Fjd6TvnYE-faHiOpRlPVQcY_-tA4A"
        }
      },
      {
        "id": "did:example:123456789abcdefghi#keys-1",
        "type": "Ed25519VerificationKey2020",
        "controller": "did:example:pqrstuvwxyz0987654321",
        "publicKeyMultibase": "zH3C2AVvLMv6gmMNam3uVAjZpfkcJCwDwnZn6z3wXmqPV"
      }
    ],
    "authentication": [
      "did:example:@@@@@@@@@@@@@@@@@@@@@@@@#keys-1",
      {
        "id": "did:example:123456789abcdefghi#keys-2",
        "type": "Ed25519VerificationKey2020",
        "controller": "did:example:123456789abcdefghi",
        "publicKeyMultibase": "zH3C2AVvLMv6gmMNam3uVAjZpfkcJCwDwnZn6z3wXmqPV"
      }
    ],
    "service" : [ 
      {
        "id" : "did:example:123456789abcdefghi#did-communication",
        "type" : ["ServiceEndpointProxyService"],
        "serviceEndpoint" : ["https://myservice.com/myendpoint"]
      },
      {
        "id":"did:example:123#linked-domain",
        "type": "LinkedDomains", 
        "serviceEndpoint": "https://bar.example.com"
      }
    ]
  }
}
  """
  
  /* val service = Service
    .builder()
    .`type`("ServiceEndpointProxyService")
    .serviceEndpoint("https://myservice.com/myendpoint")
    .build()
  val verificationMethod = VerificationMethod
    .builder()
    .id(URI.create(didString + "#key-1"))
    .`type`("Ed25519VerificationKey2018")
    .publicKeyBase58("FyfKP2HvTKqDZQzvyL38yXH7bExmwofxHf2NR5BrcGf1")
    .build()
  val didDocBuilder = DIDDocument
    .builder()
    .id(did)
    .service(service)
    .verificationMethod(verificationMethod)
    .build() */
  /* val ALICE_DID = "did:example:alice"
  val aliceDID = URI.create(ALICE_DID)
  val BOB_DID = "did:example:bob"
  val bobDIF = URI.create(BOB_DID)
  val CHARLIE_DID = "did:example:charlie"
  val charlieDID = URI.create(CHARLIE_DID) */

  test("DIDDoc should be encoded to JSON") {
    import dev.mn8.gleibnif.CirceDIDCodec.*

    val didDocJsonString = parse(didDocJson) match {
      case Left(failure) => 
        println(s"Invalid JSON String :( $failure)")
      case Right(json) => 
        val dDoc = json.as[DIDDoc]
        println("\nJSonString:\n" + json)
        dDoc match 
          case Left(failure) => 
            println(s"Failed decoding Json :( $failure)")
          case Right(didDoc) => 
            println("\nJSonString as DIDDoc:\n" + didDoc)
            println("\nDIDDoc as JSonString:\n" + didDoc.asJson.spaces2)
    }

   /*  println("\nDIDDocBuilder:\n" + didDocBuilder.toJson(true))
    val didDocBuilderJson = parse(didDocBuilder.toJson(true)) match {
      case Left(failure) => 
        println("Invalid JSON Build :(")
      case Right(json) => 
        val dd = json.as[DIDDoc]
      
        println("\nJSonBuilder as DIDDoc:\n" + dd)
        println("\nJSon Build:\n" + json)
    } */
   



   /*  val didJWT = "did:jwk:eyJraWQiOiJ1cm46aWV0ZjpwYXJhbXM6b2F1dGg6andrLXRodW1icHJpbnQ6c2hhLTI1NjpGZk1iek9qTW1RNGVmVDZrdndUSUpqZWxUcWpsMHhqRUlXUTJxb2JzUk1NIiwia3R5IjoiT0tQIiwiY3J2IjoiRWQyNTUxOSIsImFsZyI6IkVkRFNBIiwieCI6IkFOUmpIX3p4Y0tCeHNqUlBVdHpSYnA3RlNWTEtKWFE5QVBYOU1QMWo3azQifQ"
    val didSov = "did:sov:WRfXPg8dantKVubE3HX8pw"
    val x = ResolverServiceClient.resolve(didJWT)

    val y = ResolverServiceClient.resolve(didSov)
 */


  }

    // val boxes: Seq[Wallet.Box] = Seq(new Wallet.Box("",None))
    // FlowSpec(name, parameters, wallets, transactions)

    /* val name = "didcomm"
    val parameters: Seq[Param] = Seq(
      Param("playCount", "Integer"),
      Param("p1PK", "String"),
      Param("p2PK", "String")
    )
    val wallets: Seq[Wallet] = Seq(
      Wallet(
        "player1",
        "pk",
        Seq(Wallet.Box("funds", None))
      ),
      Wallet(
        "player2",
        "pk",
        Seq(Wallet.Box("funds", None))
      ),
      Wallet(
        "game",
        "pk",
        Seq(
          Wallet.Box(
            "game",
            Some(ErgCondition("targetBoxName", "erg_expression")),
            Seq(TokenCondition("tokeName", "targetBoxName", "expression")),
            Seq(Condition("targetBoxName", "expression"))
          )
        )
      ),
      Wallet(
        "state",
        "pk",
        Seq(Wallet.Box("init", None), Wallet.Box("end", None))
      )
    )
    val transactions = Seq(
      Transaction(
        name = "provide funds",
        inputs = Seq(
          InputArrow("state", "init", Some(SpendingPath("action", "Condition")))
        )
      ),
      Transaction(
        name = "play game",
        inputs = Seq(
          InputArrow(
            "p2Choice",
            "fromBox",
            Some(SpendingPath("action", "Condition"))
          )
        )
      ),
      Transaction(
        name = "create game",
        inputs = Seq(
          InputArrow(
            "p2Choice",
            "fromBox",
            Some(SpendingPath("action", "Condition"))
          )
        )
      ),
      Transaction(
        name = "withdraw",
        inputs = Seq(
          InputArrow(
            "p2Choice",
            "fromBox",
            Some(SpendingPath("action", "Condition"))
          )
        )
      )
    ) */
  
}
