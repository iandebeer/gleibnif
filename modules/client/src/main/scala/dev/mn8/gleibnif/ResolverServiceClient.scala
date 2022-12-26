package dev.mn8.gleibnif

import org.didcommx.didcomm.diddoc.DIDDocResolver
import java.util.Optional
import scala.jdk.OptionConverters
import sttp.client3.*
import sttp.client3.circe.*

import io.circe.*
import io.circe.parser.*
import dev.mn8.gleibnif.DIDDoc
import dev.mn8.gleibnif.CirceDIDCodec.decodeDIDDoc


object ResolverServiceClient { // extends DIDDocResolver {


 // given didDecoder: Decoder[DIDDoc] = deriveDecoder[dev.mn8.gleibnif.DIDDoc]
  def resolve(did: String): Optional[DIDDoc] =
    val query = "http language:scala"
    val request =
      basicRequest.get(uri"https://dev.uniresolver.io/1.0/identifiers/$did")
    val backend = HttpClientSyncBackend()
    val response = request.send(backend)
   
    response.body match 
      case Left(e) => 
        println(e)
        Optional.empty[DIDDoc]

      case Right(b) => 
        parse(b) match 
        case Left(failure) => 
          println("Invalid JSON :(")
          Optional.empty[DIDDoc]

        case Right(json) => 
          println("\nJSON:\n" + json)
          println("\nDIDDIC\n" + json.as[DIDDoc])
        
          Optional.empty[DIDDoc]

}
