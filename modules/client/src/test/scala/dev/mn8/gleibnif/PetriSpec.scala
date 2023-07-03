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
import cats.effect.*
import cats.syntax.all.*

import scodec.bits.*

import scala.collection.immutable.ListSet
import scala.io.Source
import scala.quoted.*
import cats.effect._
import cats.syntax.all._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import sttp.tapir.docs.openapi._
import scala.concurrent.ExecutionContext
import java.util.UUID
import dev.mn8.gleibnif.PetriRunner

object DWNContext:
  class DID(val did: String) 
  case class ContextID(contextId: UUID) 

  case class DwnContext(did: DID, id: ContextID)

    
class PetriSpec extends CatsEffectSuite {
  //implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

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
    //PetriRunner("purchase")
  }
}
