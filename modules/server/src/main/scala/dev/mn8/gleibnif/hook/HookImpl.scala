package dev.mn8.gleibnif.hook

import cats.effect.IO
import dev.mn8.dwn.dwn_service.CommonStatus
import dev.mn8.dwn.dwn_service.Status
import dev.mn8.dwn.dwn_service.*
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final case class HookImpl():

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  def log[T](value: T)(implicit logger: Logger[IO]): IO[Unit] =
    logger.info(s"OpenAIAgent: $value")

  def registerHook(request: RegisterHookRequest): IO[RegisterHookResponse] =
    println(s"registerHook: ${request.message}")
    parse(request.message.toString()) match
      case Left(e) =>
        log(s"error: $e")
        IO.delay(
          RegisterHookResponse(
            Some(
              CommonStatus(
                status = Status.ERROR,
                details = s"error: $e".asJson.noSpaces
              )
            )
          )
        )
      case Right(hook) =>
        log(s"hook: $hook")
        IO.delay(
          RegisterHookResponse(
            Some(CommonStatus(status = Status.OK, details = "hook registered"))
          )
        )

  def updateHook(request: UpdateHookRequest)                             = ???
  def getHooksForRecord(request: GetHooksForRecordRequest)               = ???
  def getHookByRecordId(request: GetHookByRecordIdRequest)               = ???
  def notifyHooksOfRecordEvent(request: NotifyHooksOfRecordEventRequest) = ???
