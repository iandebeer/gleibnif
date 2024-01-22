package dev.mn8.gleibnif

import _root_.io.grpc.ForwardingServerCall.SimpleForwardingServerCall
import _root_.io.grpc.*
import cats.effect.*
import com.google.api.http.Http
import dev.mn8.dwn.dwn_service.RecordServiceGrpc.RecordServiceStub
import dev.mn8.dwn.dwn_service.*
import dev.mn8.gleibnif.hook.HookImpl
import fs2.grpc.server.ServerOptions
import fs2.grpc.syntax.all._
import fs2.grpc.syntax.all._
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.Await
import scala.concurrent.ExecutionContext

import Constants._

case class AuthInterceptor(msg: String = "hello") extends ServerInterceptor:
  override def interceptCall[Req, Res](
      call: ServerCall[Req, Res],
      requestHeaders: Metadata,
      next: ServerCallHandler[Req, Res]
  ) =
    println(s"$msg: ${requestHeaders.get(Constants.AuthorizationMetadataKey)}")
    next.startCall(call, requestHeaders)

class HookServiceImpl(using logger: org.log4s.Logger) extends HookServiceFs2Grpc[IO, Metadata] {
  def log[T](value: T)(implicit logger: org.log4s.Logger) =
    logger.info(s"HookServiceImpl: $value")
  override def updateHook(
      request: UpdateHookRequest,
      ctx: Metadata
  ): IO[UpdateHookResponse] =
    log(s"updateHook: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(UpdateHookResponse())

  override def getHooksForRecord(
      request: GetHooksForRecordRequest,
      ctx: Metadata
  ): IO[GetHooksForRecordResponse] =
    log(s"getHooksForRecord: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(GetHooksForRecordResponse())

  override def getHookByRecordId(
      request: GetHookByRecordIdRequest,
      ctx: Metadata
  ): IO[GetHookByRecordIdResponse] =
    log(s"getHookByRecordId: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(GetHookByRecordIdResponse())

  override def notifyHooksOfRecordEvent(
      request: NotifyHooksOfRecordEventRequest,
      ctx: Metadata
  ): IO[NotifyHooksOfRecordEventResponse] =
    log(
      s"notifyHooksOfRecordEvent: ${ctx.get(Constants.AuthorizationMetadataKey)}"
    )
    IO(NotifyHooksOfRecordEventResponse())

  override def registerHook(
      request: RegisterHookRequest,
      ctx: Metadata
  ): IO[RegisterHookResponse] =
    log(s"registerHook: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    HookImpl().registerHook(request)
}

class KeyServiceImpl(using logger: org.log4s.Logger) extends KeyServiceFs2Grpc[IO, Metadata]:
  def log[T](value: T)(implicit logger: org.log4s.Logger) =
    logger.info(s"HookSeKeyServiceImplrviceImpl: $value")
  def verifyMessageAttestation(
      request: VerifyMessageAttestationRequest,
      ctx: Metadata
  ): IO[VerifyMessageAttestationResponse] =
    log(
      s"verifyMessageAttestation: ${ctx.get(Constants.AuthorizationMetadataKey)}"
    )
    IO(VerifyMessageAttestationResponse())

class RecordServiceImpl(using logger: org.log4s.Logger) extends RecordServiceFs2Grpc[IO, Metadata]:
  def log[T](value: T)(implicit logger: org.log4s.Logger) =
    logger.info(s"HookServiceImpl: $value")
  def createSchema(
      request: CreateSchemaRequest,
      ctx: Metadata
  ): IO[CreateSchemaResponse] =
    log(s"createSchema: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(CreateSchemaResponse())
  override def storeRecord(
      request: StoreRecordRequest,
      ctx: Metadata
  ): IO[StoreRecordResponse] =
    logger.info(s"storeRecord: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(StoreRecordResponse())
  override def findRecord(
      request: FindRecordRequest,
      ctx: Metadata
  ): IO[FindRecordResponse] =
    log(s"findRecord: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(FindRecordResponse())
  override def validateRecord(
      request: ValidateRecordRequest,
      ctx: Metadata
  ): IO[ValidateRecordResponse] =
    log(s"validateRecord: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(ValidateRecordResponse())
  override def invalidateSchema(
      request: InvalidateSchemaRequest,
      ctx: Metadata
  ): IO[InvalidateSchemaResponse] =
    log(s"invalidateSchema: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(InvalidateSchemaResponse())

  def storeRecord(request: Record, metadata: Metadata): IO[Record] =
    log(s"storeRecord: ${metadata.get(Constants.AuthorizationMetadataKey)}")
    IO(request)

object Main extends IOApp.Simple:
  import scala.jdk.CollectionConverters.*
  given logger: org.log4s.Logger =
    org.log4s.getLogger // Slf4jLogger.getLogger[IO]
  def log[T](value: T)(implicit logger: org.log4s.Logger) =
    logger.info(s"Main: $value")

  val recordService: Resource[IO, ServerServiceDefinition] =
    RecordServiceFs2Grpc
      .bindServiceResource[IO](new RecordServiceImpl, ServerOptions.default)
  val keyService: Resource[IO, ServerServiceDefinition] =
    KeyServiceFs2Grpc
      .bindServiceResource[IO](new KeyServiceImpl, ServerOptions.default)

  val hookService: Resource[IO, ServerServiceDefinition] =
    HookServiceFs2Grpc
      .bindServiceResource[IO](new HookServiceImpl, ServerOptions.default)
  val services =
    for
      r <- recordService.use(s => IO(s))
      k <- keyService.use(s => IO(s))
      h <- hookService.use(s => IO(s))
    yield List(r, k, h)

  def run: IO[Unit] =
    val mySync: Async[IO] = Async[IO]
    log("starting server")
    val startup: IO[Any] =
      for
        _ <- IO(println("starting server"))
        s <- services
        _ <- recordService.use { (service: ServerServiceDefinition) =>
          ServerBuilder
            .forPort(9999)
            .addServices(s.asJava)
            .intercept(AuthInterceptor("hi there: "))
            .resource[IO](mySync)
            .evalMap(server => IO(server.start()))
            .useForever
        }
      yield ()
    startup >> IO.unit
