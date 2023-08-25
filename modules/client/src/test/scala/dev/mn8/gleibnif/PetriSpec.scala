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
import dev.mn8.gleibnif.PetriCompiler
import fs2.grpc.syntax.serverBuilder
import scodec.bits.{Bases, BitVector, ByteOrdering}

object DWNContext:
  class DID(val did: String)
  case class ContextID(contextId: UUID)

  case class DwnContext(did: DID, id: ContextID)

class PetriSpec extends CatsEffectSuite {
  // implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  given logger: org.log4s.Logger = org.log4s.getLogger

  import DWNContext.*
  val s = PetriCompiler("purchase")
  val cpn = s.cpn
  test("build net") {

    PetriPrinter(fileName = "purchase", petriNet = cpn).print()
    val m = Markers(cpn)
    println(s"\nmarkers = $m")
    println(s"colourMap -> ${s.colourMap}")
    println(s"params -> ${s.paramValues}")
    println(s"paramMap -> ${s.paramMap}")
    /*    PetriPrinter(fileName = "purchase1", petriNet = cpn).print()
    val places = cpn.elements.values.collect { case p: Place => p }
    val dimensions = (places.size, places.maxBy(p => p.capacity).capacity)
    println(s"dimensions of marker array: $dimensions")
    //val m1 = Markers(pn)
    //println(s"\nm1 = \n${m1}\n${m1.toStateVector} => ${m1.serialize}")
    // 000 000 000 000 000 000
    //4AA=
    val st1 = bin"00000000000011".toBase64
    val st2 = bin"00000000000011".toBase64
    val st3 = bin"00000001111111".toBase64
    val st4 = bin"00000000011100".toBase64
    val st5 = bin"00000000000000".toBase64
    println(s"bit array = \n$st1\n$st2\n$st3\n$st4\n$st5")
    val ser1 = Markers(cpn,st1).serialize
    //HAA=
    val ser2 = Markers(cpn,bin"0000011111000000000000000").serialize
    //A4A=
    val ser3 = Markers(cpn,bin"0000000000111110000000000").serialize
    val ser4 = Markers(cpn,bin"0000000000000001111100000").serialize

    val ser5 = Markers(cpn,bin"0000000000000000000011111").serialize


    println(s"serialize -> $ser1\n$ser2\n$ser3,\n$ser4\n$ser5")
    val x1 = cpn.peek(Step(Markers(cpn,st1)))
    val x2 = cpn.peek(Step(Markers(cpn,st2)))
    val x3 = cpn.peek(Step(Markers(cpn,st3)))
    val x4 = cpn.peek(Step(Markers(cpn,st4)))
    val x5 = cpn.peek(Step(Markers(cpn,ser5)))





    println(s"peek -> \n$x1\n$x2\n$x3\n$x4\n$x5") */

  }
  test("create a server with end points for each param") {
    val st1 = bin"00000000000011".toBase64
    val st2 = bin"0000000000000000000000011".toBase64
    val st3 = bin"00000001111111".toBase64
    val st4 = bin"00000000011100".toBase64
    val st5 = bin"00000000000000".toBase64
    val places = cpn.elements.values.collect { case p: Place => p }
    val start: NodeId = places.find(_.name == "start").get.id
    println(s"places = ${places.mkString(",")}")
    val dimensions = (places.size, places.maxBy(p => p.capacity).capacity)
    println(s"dimensions of marker array: $dimensions")
    println(s"bit array = \n$st1\n$st2\n$st3\n$st4\n$st5")
    val m0 = Markers(cpn).setMarker(Marker(start, bin"11"))
    println(s"at start = ${m0.serialize}")
    val m1 = Markers(cpn, st1)
    PetriPrinter(fileName = "petrinet0", petriNet = cpn).print(None)
    println("p0 = " + cpn.peek(Step(m0)))
    val steps: State[Step, Unit] = for
      p0 <- cpn.step
      p1 <- cpn.step
      p2 <- cpn.step
      p3 <- cpn.step
    yield (
      PetriPrinter(fileName = "petrinet1", petriNet = cpn).print(Option(p0)),
      //  println(s"p0 state = ${p0.state} \n=> ${p0.serialize} \n=> ${p0.serialize}"),
      println("p0 = " + cpn.peek(Step(p0, 1))),
      PetriPrinter(fileName = "petrinet2", petriNet = cpn).print(Option(p1)),
      println("p1 = " + cpn.peek(Step(p1, 2))),
      //  println(s"p1 state = ${p1.state} \n=> ${p1.serialize} \n=> ${p1.serialize}"),
      PetriPrinter(fileName = "petrinet3", petriNet = cpn).print(Option(p2)),
      println("p2 = " + cpn.peek(Step(p2, 3))),
      //  println(s"p2 state = ${p2.state} \n=> ${p2.serialize} \n=> ${p2.serialize}"),
      PetriPrinter(fileName = "petrinet4", petriNet = cpn).print(Option(p3)),
      //  println(s"p1 state = ${p1.state} \n=> ${p1.serialize} \n=> ${p1.serialize}"),

    )
    steps.run(Step(m0, 0)).value
  }
}
