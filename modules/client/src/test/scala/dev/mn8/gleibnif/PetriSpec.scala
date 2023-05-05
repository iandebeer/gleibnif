package dev.mn8.gleipnif
import cats.data.State
import cats.syntax.functor.*
import dev.mn8.castanet.*
import io.circe.Decoder
import io.circe.Encoder
import io.circe.*
import io.circe.generic.auto.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.syntax.*
import munit.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scodec.bits.*

import scala.collection.immutable.ListSet
import scala.io.Source
import scala.quoted.*
import cats.effect.IO

class PetriSpec extends FunSuite {
  test("build net") {
    given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
    val cpn = new Services().buildPetriNet("purchase")
    PetriPrinter(fileName = "purchase", petriNet = cpn).print()

  }
}
