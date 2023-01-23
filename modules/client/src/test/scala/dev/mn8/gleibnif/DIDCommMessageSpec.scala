package dev.mn8.gleibnif

import munit.*
import io.circe.*
import io.circe.syntax.*

import io.circe.parser.*
import dev.mn8.gleibnif.DIDCommMessage
import dev.mn8.gleibnif.DIDCommCodec.decodeDIDCommMessage
import dev.mn8.gleibnif.DIDCommCodec.encodeDIDCommMessage

import sttp.client3.*


class DIDCommMessageSpec extends FunSuite {
  
  val jsonString = """ 
 {
  "id": "1234567890",
  "type": "<sometype>",
  "to": [
    "did:example:mediator"
  ],
  "body": {
    "attachment_id": "1",
    "encrypted_details": {
      "id": "x",
      "encrypted_to": "",
      "other_details": "about attachment"
    }
  },
  "attachments": [
    {
      "id": "1",
      "description": "example b64 encoded attachment",
      "data": {
        "base64": "WW91ciBob3ZlcmNyYWZ0IGlzIGZ1bGwgb2YgZWVscw=="
      }
    },
    {
      "id": "2",
      "description": "example linked attachment",
      "data": {
        "hash": "<multi-hash>",
        "links": [
          "https://path/to/resource"
        ]
      }
    },
    {
      "id": "x",
      "description": "example encrypted DIDComm message as attachment",
      "media_type": "application/didcomm-encrypted+json",
      "data": {
        "json": {
          "test": "this is a test",
          "nested": {
            "test": "this is a nested test"
          }

        }
      }
    }
  ]
}
"""
  
test("DIDCommMessage should be encoded to JSON") {
    import dev.mn8.gleibnif.DIDCodec.* 
    println("\n\n*******************\nDIDCommMessage as JSON:\n*******************\n")
    testParse(jsonString)
    
  }
  def testParse(jsonString:String) =
     val didDocJsonString = parse(jsonString) match {
      case Left(failure) => 
        println(s"Invalid JSON String :( $failure)")
      case Right(json) => 
        val dDoc = json.as[DIDCommMessage]
        println("\nJSonString:\n" + json)
        dDoc match 
          case Left(failure) => 
            println(s"Failed decoding Json :( $failure)")
          case Right(didDoc) => 
            println("\nJSonString as DWNMessage:\n" + didDoc)
            println("\nDIDDoc as JSonString:\n" + didDoc.asJson.spaces2)
    }
  }