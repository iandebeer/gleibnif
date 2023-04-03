package dev.mn8.gleibnif

import munit.*

import java.net.URI
import io.circe.*
import io.circe.syntax.*

import io.circe.parser.*
import dev.mn8.gleibnif.DIDDoc
import dev.mn8.gleibnif.DIDCodec.decodeDIDDoc
import dev.mn8.gleibnif.DIDCodec.encodeDIDDoc

import sttp.client3.*
import java.io.StringReader

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

import com.apicatalog.jsonld.JsonLd
import java.io.Reader
import com.apicatalog.jsonld.document.JsonDocument
import dev.mn8.gleibnif.jsonld.JsonLDP
import cats.effect.IO
import cats.effect.unsafe.implicits._


class DidDocSpec extends FunSuite {

  val did = uri"did:ex:1234"
  val didString: String = did.toString()
  val didDocJson = """
{
   "id": "did:example:123456789abcdefghi",
    "controller": "did:example:bcehfew7h32f32h7af3",
    "alsoKnownAs": [
      "did:example:bcehfew7h32f32h7af3"
    ],
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
    "service": [
      {
        "id": "did:example:123456789abcdefghi#did-communication",
        "type": [
          "ServiceEndpointProxyService"
        ],
        "serviceEndpoint": [
          "https://myservice.com/myendpoint"
        ]
      },
      {
        "id": "did:example:123#linked-domain",
        "type": "LinkedDomains",
        "serviceEndpoint": "https://bar.example.com"
      },
      {
        "id": "did:example:123456789abcdefghi#didcomm-1",
        "type": "DIDCommMessaging",
        "serviceEndpoint": [
          {
            "uri": "https://example.com/path",
            "accept": [
              "didcomm/v2",
              "didcomm/aip2;env=rfc587"
            ],
            "routingKeys": [
              "did:example:somemediator#somekey"
            ]
          }
        ]
      },
      {
        "id": "did:example:123456789abcdefghi#didcomm-1",
        "type": "DIDCommMessaging",
        "serviceEndpoint": [
          {
            "uri": "did:example:somemediator"
          }
        ]
      },
      {
        "id": "did:example:123456789abcdefghi#didcomm-1",
        "type": "DIDCommMessaging",
        "serviceEndpoint": [
          {
            "uri": "did:example:somemediator",
            "routingKeys": [
              "did:example:anothermediator#somekey"
            ]
          }
        ]
      }
    ]
}  
  """

  val contextLD = """ {"@context": [
      "https://www.w3.org/ns/did/v1",
      "https://w3id.org/security/suites/jws-2020/v1",
      "https://w3id.org/security/suites/ed25519-2020/v1"
    ]}
  """
  val jsonLDString = """
{
    "@context": [
      "https://www.w3.org/ns/did/v1",
      "https://w3id.org/security/suites/jws-2020/v1",
      "https://w3id.org/security/suites/ed25519-2020/v1"
    ],
    "id": "did:example:123456789abcdefghi",
    "controller": "did:example:bcehfew7h32f32h7af3",
    "alsoKnownAs": [
      "did:example:bcehfew7h32f32h7af3"
    ],
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
    "service": [
      {
        "id": "did:example:123456789abcdefghi#did-communication",
        "type": [
          "ServiceEndpointProxyService"
        ],
        "serviceEndpoint": [
          "https://myservice.com/myendpoint"
        ]
      },
      {
        "id": "did:example:123#linked-domain",
        "type": "LinkedDomains",
        "serviceEndpoint": "https://bar.example.com"
      },
      {
        "id": "did:example:123456789abcdefghi#didcomm-1",
        "type": "DIDCommMessaging",
        "serviceEndpoint": [
          {
            "uri": "https://example.com/path",
            "accept": [
              "didcomm/v2",
              "didcomm/aip2;env=rfc587"
            ],
            "routingKeys": [
              "did:example:somemediator#somekey"
            ]
          }
        ]
      },
      {
        "id": "did:example:123456789abcdefghi#didcomm-1",
        "type": "DIDCommMessaging",
        "serviceEndpoint": [
          {
            "uri": "did:example:somemediator"
          }
        ]
      },
      {
        "id": "did:example:123456789abcdefghi#didcomm-1",
        "type": "DIDCommMessaging",
        "serviceEndpoint": [
          {
            "uri": "did:example:somemediator",
            "routingKeys": [
              "did:example:anothermediator#somekey"
            ]
          }
        ]
      }
    ]
  }

"""
  
  /* val ALICE_DID = "did:example:alice"
  val aliceDID = URI.create(ALICE_DID)
  val BOB_DID = "did:example:bob"
  val bobDIF = URI.create(BOB_DID)
  val CHARLIE_DID = "did:example:charlie"
  val charlieDID = URI.create(CHARLIE_DID) */



  val apiKey = "c2850992-32fd-4ccf-9352-77aa329eef13"
  val baseURL = "https://api.godiddy.com/0.1.0/universal-resolver/identifiers/"

  def testParse(jsonString:String) =
     val didDocJsonString = parse(jsonString) match {
      case Left(failure) => 
        println(s"Invalid JSON String :( $failure)")
      case Right(json) => 
        val dDoc = json.as[DIDDoc]
        dDoc match 
          case Left(failure) => 
            println(s"Failed decoding Json :( $failure)")                     
          case Right(didDoc) => 
            println("\nDIDDoc as JSonString:\n" + didDoc.asJson.spaces2)
    }

 /*  test("Resolve a did") {
    val r = for 
      x <- ResolverServiceClient(baseURL,apiKey).resolve("did:indy:danube:7vZbRUJtepc9ct8KPUuQNn") 
      _ <- x match
        case Left(failure) => 
          IO(println(s"Failed resolving DID :( $failure)"))
        case Right(didDoc: Any) => 
          IO(println("\nDIDDoc as JSonString:\n" + didDoc.asJson.spaces2))
    yield()
    r.flatTap(m => IO(println(s"$r"))).unsafeRunSync()
  } */

  test("ResolveToJson a did") {
    val r = for 
      x <- ResolverServiceClient(baseURL,apiKey).resolveToJson("did:indy:danube:7vZbRUJtepc9ct8KPUuQNn") 
      _ <- x match
        case Left(failure) => 
          IO(println(s"Failed resolving DID :( $failure)"))
        case Right(didDoc: String) => 
          IO(println("\n"))
    yield()
    r.flatTap(m => IO(println(s"$r"))).unsafeRunSync()
  }

  test("add template document") {
    val doc = DIDDoc("",Some("did:example:123456789"),Some(Set("tel:12345k;name=Ian de Beer")),None,None,None,None,None,None,
    Some(Set(Service(id= new URI("#dwn"), `type`= Set("DecentralizedWebNode"), serviceEndpoint=Set(ServiceEndpointNodes(
    nodes=Set(new URI("https://dwn.example.com"), new URI("https://example.org/dwn"))))))))
    println(doc.asJson.spaces2)

  }
 /*  test("JSONLD should be encoded to JSON") {
    //import dev.mn8.gleibnif.DIDCodec.* 
    println("\n\n*******************\nDIDDoc as JSON:\n*******************\n")
    testParse(didDocJson)
    println("\n\n******************************************************************\n")

    
  } */

  /* test("Jsonldp must process jsonld docs") {
    println("\n\n******************* JSONLD Processor: *******************\n")

    parse(jsonLDString) match {
      case Left(failure) => 
        println(s"Invalid JSON String :( ${failure.message}")
      case Right(json) => 
        JsonLDP(json).expand() match
          case Left(failure) => 
            println(s"${failure.message}")
          case Right(value) =>
            println(value.json.spaces2)
            println("\n\n******************************************************************\n")

            value.compact(new URI("file:/Users/ian/dev/gleibnif/modules/client/src/test/resources/jsonLdContext.json")) match
              case Left(value) => println(value)
              case Right(value) => println(value.json.spaces2) 
    }
  } */
}