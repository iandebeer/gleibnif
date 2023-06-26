package dev.mn8.gleipnif
import cats.data.State
import cats.syntax.functor.*
import dev.mn8.castanet.*
import io.circe.*
import io.circe.parser.*
import io.circe.parser.decode
import io.circe.syntax.*
import munit.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scodec.bits.*

import scala.collection.immutable.ListSet
import scala.io.Source
import scala.quoted.*
import cats.effect.IO
import dev.mn8.gleibnif.PetriRunner
import sttp.tapir.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import org.http4s.HttpRoutes
import java.util.UUID
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import java.net.URI
import sttp.tapir.swagger.bundle.SwaggerInterpreter

//import org.http4s.blaze.server.BlazeServerBuilder
//import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.docs.openapi._
import sttp.tapir.swagger.http4s.SwaggerHttp4s

object DWNContext:
  class DID(val did: String) 
  case class ContextID(contextId: UUID) 

  case class DwnContext(did: DID, id: ContextID)

    
class PetriSpec extends FunSuite {
  import DWNContext.*
  test("build net") {
    given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

    val s = PetriRunner("purchase")
    val cpn = s.cpn
    PetriPrinter(fileName = "purchase", petriNet = cpn).print()
    val m = Markers(cpn)
    println(s"\nmarkers = $m")
    println(s"colourMap -> ${s.colourMap}")
    println(s"params -> ${s.paramValues}")
    println(s"paramMap -> ${s.paramMap}")
    PetriPrinter(fileName = "purchase1", petriNet = cpn).print()
    val x = s.cpn.peek(Step(Markers(cpn),1))
    println(s"peek -> $x")
    
  }
  test("create a server with end points for each param") {
    val input: EndpointInput[(String,UUID)] = query[String]("did").and(query[UUID]("context"))
    val x: ServerEndpoint[Any, IO]{type SECURITY_INPUT >: Unit <: Unit; type PRINCIPAL >: Unit <: Unit; type INPUT >: (String, String, UUID) <: (String, String, UUID); type ERROR_OUTPUT >: Unit <: Unit; type OUTPUT >: String <: String} = endpoint.get
      .name("dawn")
      .in(path[String]("dwn"))
      .in(input)
      .out(stringBody)
      .serverLogic { context =>
        IO.pure(Right(s"D@WN-query, from ${context._1} contextId ${context._2}"))
      }
    val openApiDocs: String = List(x).toOpenAPI("The User API", "1.0").toYaml
    val swaggerRoutes: HttpRoutes[IO] = new SwaggerHttp4s(openApiDocs).routes[IO]
    
   // val z =  SwaggerInterpreter().fromEndpoints(List(x), "My App", "1.0")

    println(s"end point -> ${x.showDetail}")  
    println(s"routes -> ${swaggerRoutes}")


  }
}
