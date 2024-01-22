package dev.mn8.gleibnif

import pureconfig.*
import pureconfig.generic.derivation.default.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.effect.IO
import eu.timepit.refined.*
import eu.timepit.refined.pureconfig.*
import cats.effect.kernel.Sync
import cats.effect._
import cats.implicits._

object ConfigManager {
  given Logger[IO] = Slf4jLogger.getLogger[IO]
  def info[F[_]: Sync: Logger](value: String) =
    for
      _         <- Logger[F].info("Logging at start of passForEasierUse")
      something <- Sync[F].delay(println("I could do anything"))
      // .onError{case e => Logger[F].error(e)("Something Went Wrong in passForEasierUse")}
      _ <- Logger[F].info(s"$value")
    yield something

  // println(s"Main: $value")
  // logger.info(s"$value"

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

  def protocolConf(interfaceName: String): ProtocolConf =
    ConfigSource.default.at(s"$interfaceName-proto").load[ProtocolConf] match
      case Left(error) =>
        err(s"Error: $error")
        ProtocolConf(List(), List(), StartConf("", 0, List()), "", List())
      case Right(conf) => conf

}
