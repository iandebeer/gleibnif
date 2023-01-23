package dev.mn8.gleibnif

import munit.*
import io.circe.*
import io.circe.syntax.*

import io.circe.parser.*
import dev.mn8.gleibnif.DIDDoc
import dev.mn8.gleibnif.DWNodeCodec.decodeDWNodeMessage
import dev.mn8.gleibnif.DWNodeCodec.encodeDWNodeMessage

import sttp.client3.*


class DWNodeSpec extends FunSuite  {
  
  val jsonString = """ 
{
  "messages": [ 
    {
      "data": "bafybeigdyrzt5sfp7udm7hu76uh7y26nf3efuylqabf3oclgtqy55fbzdi",
      "recordId": "b65b7r8n7bewv5w6eb7r8n7t78yj7hbevsv567n8r77bv65b7e6vwvd67b6",
      "descriptor": {
        "method": "CollectionsWrite",
        "schema": "https://schema.org/SocialMediaPosting",
        "dataCid": "QmY7Yh4UquoXHLPFo2XbhXkhBvFoPwmQUSa92pxnxjQuPU",
        "dateCreated": 123456789,
        "dataFormat": "application/json"
      },
      "processing": {
        "nonce": "4572616e48616d6d65724c61686176",
        "author": "did:example:alice",
        "recipient": "did:example:bob"
      },
      "attestation": {
        "payload": "89f5hw458fhw958fq094j9jdq0943j58jfq09j49j40f5qj30jf",
        "signatures": [{
          "protected": "4d093qj5h3f9j204fq8h5398hf9j24f5q9h83402048h453q",
          "signature": "49jq984h97qh3a49j98cq5h38j09jq9853h409jjq09h5q9j4"
        }]
      },
      "authorization": {
        "payload": "bafybeigdyrzt5sfp7udm7hu76uh7y26nf3efuylqabf3oclgtqy55fbzdi",
        "signatures": [{
          "protected": "f454w56e57r68jrhe56gw45gw35w65w4f5i54c85j84wh5jj8h5",
          "signature": "5678nr67e56g45wf546786n9t78r67e45657bern797t8r6e5"
        }]
      }
    },
    {
      "data": "bafybeigdyrzt5sfp7udm7hu76uh7y26nf3efuylqabf3oclgtqy55fbzdi",
      "recordId": "b65b7r8n7bewv5w6eb7r8n7t78yj7hbevsv567n8r77bv65b7e6vwvd67b6",
      "descriptor": {
        "method": "CollectionsWrite",
        "schema": "https://schema.org/SocialMediaPosting",
        "dataCid": "QmY7Yh4UquoXHLPFo2XbhXkhBvFoPwmQUSa92pxnxjQuPU",
        "dateCreated": 123456789,
        "dataFormat": "application/json"
      },
      "processing": {
        "nonce": "4572616e48616d6d65724c61686177",
        "author": "did:example:alice",
        "recipient": "did:example:alice"
      },
      "attestation": {
        "payload": "89f5hw458fhw958fq094j9jdq0943j58jfq09j49j40f5qj30jf",
        "signatures": [{
          "protected": "4d093qj5h3f9j204fq8h5398hf9j24f5q9h83402048h453q",
          "signature": "49jq984h97qh3a49j98cq5h38j09jq9853h409jjq09h5q9j4"
        }]
      },
      "authorization": {
        "payload": "bafybeigdyrzt5sfp7udm7hu76uh7y26nf3efuylqabf3oclgtqy55fbzdi",
        "signatures": [{
          "protected": "f454w56e57r68jrhe56gw45gw35w65w4f5i54c85j84wh5jj8h5",
          "signature": "5678nr67e56g45wf546786n9t78r67e45657bern797t8r6e5"
        }]
      }
    }
  ]
}
"""

  test("DIDDoc should be encoded to JSON") {
    import dev.mn8.gleibnif.DIDCommCodec.* 
    println("\n\n*******************\nDIDDoc as JSON:\n*******************\n")
    testParse(jsonString)
    
  }
  def testParse(jsonString:String) =
     val didDocJsonString = parse(jsonString) match {
      case Left(failure) => 
        println(s"Invalid JSON String :( $failure)")
      case Right(json) => 
        val dDoc = json.as[DWNodeMessage]
        println("\nJSonString:\n" + json)
        dDoc match 
          case Left(failure) => 
            println(s"Failed decoding Json :( $failure)")
          case Right(didDoc) => 
            println("\nJSonString as DWNMessage:\n" + didDoc)
            println("\nDIDDoc as JSonString:\n" + didDoc.asJson.spaces2)
    }

}
