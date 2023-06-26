package dev.mn8.gleibnif

import dev.mn8.castanet.ColouredPetriNet
import pureconfig.*
import pureconfig.generic.derivation.default.*

import scala.collection.immutable.ListSet
import dev.mn8.castanet.*
import dev.mn8.castanet.{Service => CastanetService}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import cats.data.StateT
import cats.effect.{IO, Resource}
import cats.syntax.applicative._
import cats.syntax.flatMap._
import sttp.client3.{HttpURLConnectionBackend, SttpBackend}
import sttp.model.*
import shapeless3.deriving.*
import cats.data.IndexedStateT
/**
  * PetriRunner
  * 
  *
  * @param interfaceName
  * @param logger
  */
case class PetriRunner(interfaceName: String)(using logger: Logger[IO]):

  def info[T](value: T)(using logger: Logger[IO]): IO[Unit] =
    println(s"Main: $value")
    logger.info(s"$value")

  def err[T](value: T)(using logger: Logger[IO]): IO[Unit] =
    println(s"Main: $value")
    logger.error(s"$value")

  case class WeightConf(
      start: String,
      transition: String,
      end: String,
      action: String,
      actionParams: List[String]
  ) derives ConfigReader:
    override def toString(): String =
      s"""
        |start: $start
        |transition: $transition
        |end: $end
        |action: $action
        |actionParams: $actionParams
        |""".stripMargin

  case class StartConf(
      place: String,
      weight: Int,
      initialParams: List[String]
  ) derives ConfigReader:
    override def toString(): String =
      s"""
        |place: $place
        |weight: $weight
        |initialParams: $initialParams
        |""".stripMargin

  case class ProtocolConf(
      places: List[String],
      transitions: List[String],
      start: StartConf,
      end: String,
      weights: List[WeightConf]
  ) derives ConfigReader:
    override def toString(): String =
      s"""
        |places: $places
        |transitions: $transitions
        |start: $start
        |end: $end
        |weights: $weights
        |""".stripMargin

  val protocolConf: ProtocolConf =
    ConfigSource.default.at(s"$interfaceName-proto").load[ProtocolConf] match
      case Left(error) =>
        err(s"Error: $error")
        ProtocolConf(List(), List(), StartConf("", 0, List()), "", List())
      case Right(conf) => conf
  val pl1: ListSet[String] = ListSet.from(protocolConf.weights).flatMap(w => w.actionParams)
  val pl2: ListSet[String] = ListSet.from(protocolConf.start.initialParams).flatMap(w => protocolConf.start.initialParams)
  val paramList: ListSet[String] = (pl2 ++ pl1 )

  val colourMap: Map[Colour, String] = paramList.zipWithIndex.map{case (k, v) => (Colour.fromOrdinal(v), k)}.toMap
  val paramMap: Map[String, Colour] = colourMap.map(_.swap)//(m => m._2 -> m._1)
  val paramValues = colourMap.map(  (k,v) => (k -> ""))

  val cpn: ColouredPetriNet =

    val places: Map[String, Place] = protocolConf.places.map { p =>
      val capacity: Int =
        if (p == protocolConf.start.place) then
          protocolConf.start.initialParams.length
        else
          protocolConf.weights
            .filter(w => w.start == p)
            .foldRight[Int](0)((w, l) => w.actionParams.length)
      (p -> Place(p, capacity))
    }.toMap
   // println(s"places -> $places")

    val transitions: Map[String, Transition] = protocolConf.transitions
      .map(t => (t -> Transition(t, CastanetService(), RPC(t, "", ""))))
      .toMap
    val start = protocolConf.start
    val end = protocolConf.end
    val w1 = protocolConf.weights.map { w =>
      (w.end -> ListSet.from( w.actionParams.map(p => Weight(paramMap(p),1))))}.toMap

    val inWeights = w1 + (start.place -> ListSet(
      Weight(Colour.fromOrdinal(0), start.initialParams.length)
    ))
    val y = inWeights.map((k:String, v:ListSet[Weight]) => s"${k} -> ${v.map(w => w.colour).mkString(",")}")

    val w2: Map[String, ListSet[Weight]] = protocolConf.weights.map { w =>
      (w.start -> ListSet(
        Weight(
          Colour.fromOrdinal(protocolConf.weights.indexOf(w)),
          w.actionParams.length
        )
      ))
    }.toMap
    val outWeights = w2 + (end -> ListSet(Weight(Colour.WHITE, 0)))
    val x =  outWeights.map((k:String, v:ListSet[Weight]) => s"${k} -> ${v.map(w => w.colour).mkString(",")}")
    //println(s"out -> \n${x.mkString("\n")}")
    val triples: List[PlaceTransitionTriple] = protocolConf.weights.map { w =>
      PlaceTransitionTriple(
        places(w.start),
        inWeights(w.start),
        transitions(w.transition),
        outWeights(w.end),
        places(w.end)
      )
    }
    triples.foldRight(PetriNetBuilder())((t, b) => b.add(t)).build()

  


  

 
  

