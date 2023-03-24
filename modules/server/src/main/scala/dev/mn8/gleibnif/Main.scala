package dev.mn8.gleipnif

import cats.effect.*
import _root_.io.grpc.*

import fs2.grpc.syntax.all._
import fs2.grpc.syntax.all._
import fs2.grpc.server.ServerOptions
import _root_.io.grpc.ForwardingServerCall.SimpleForwardingServerCall
import Constants._
import  dev.mn8.dwn.dwn_service.*
import scala.concurrent.ExecutionContext
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import dev.mn8.dwn.dwn_service.RecordServiceGrpc.RecordServiceStub
import scala.concurrent.Await
import com.google.api.http.Http

object GRPCGateway :

  def main(args: Array[String]): Unit = 
    val channel = NettyChannelBuilder.forAddress("localhost", 8080).usePlaintext().build()
    val service = RecordServiceGrpc.stub(channel)
    val server = new RecordServer(service)
    server.start(8081)

  




class RecordServer(service: RecordServiceStub) :
  val server = Http.defaultInstance.serve(":8081", TicketBookingGatewayService(service))
  Await.ready(server)





object RecordGatewayService :
  def apply(service: RecordServiceStub) = 
    HttpRoutes.of[Task] {

      case request @ Method.POST -> Root / "record" / user =>
        val request = StoreRecordRequest(user)
        val response = service.storeRecord(request)
        Ok(response)

    }

case class AuthInterceptor(msg: String = "hello") extends ServerInterceptor:
  override def interceptCall[Req,Res] (
      call: ServerCall[Req, Res],
      requestHeaders: Metadata,
      next: ServerCallHandler[Req, Res]) = 
        println(s"$msg: ${requestHeaders.get(Constants.AuthorizationMetadataKey)}")
        next.startCall(call,requestHeaders)

class HookServiceImpl extends HookServiceFs2Grpc[IO, Metadata] {

  override def updateHook(request: UpdateHookRequest, ctx: Metadata): IO[UpdateHookResponse] = 
    println(s"updateHook: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(UpdateHookResponse())

  override def getHooksForRecord(request: GetHooksForRecordRequest, ctx: Metadata): IO[GetHooksForRecordResponse] = 
    println(s"getHooksForRecord: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(GetHooksForRecordResponse())


  override def getHookByRecordId(request: GetHookByRecordIdRequest, ctx: Metadata): IO[GetHookByRecordIdResponse] = 
    println(s"getHookByRecordId: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(GetHookByRecordIdResponse())

  override def notifyHooksOfRecordEvent(request: NotifyHooksOfRecordEventRequest, ctx: Metadata): IO[NotifyHooksOfRecordEventResponse] = 
    println(s"notifyHooksOfRecordEvent: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(NotifyHooksOfRecordEventResponse())

  override def registerHook(request: RegisterHookRequest, ctx: Metadata): IO[RegisterHookResponse] = 
    println(s"registerHook: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(RegisterHookResponse())
} 
 
class KeyServiceImpl extends KeyServiceFs2Grpc[IO, Metadata] :

  def verifyMessageAttestation(request: VerifyMessageAttestationRequest, ctx: Metadata): IO[VerifyMessageAttestationResponse] =
    println(s"verifyMessageAttestation: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(VerifyMessageAttestationResponse())

class RecordServiceImp extends RecordServiceFs2Grpc[IO, Metadata] :
  def createSchema(request: CreateSchemaRequest, ctx: Metadata): IO[CreateSchemaResponse] =
    println(s"createSchema: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(CreateSchemaResponse())
  override def storeRecord(request: StoreRecordRequest, ctx: Metadata): IO[StoreRecordResponse] = 
    println(s"storeRecord: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(StoreRecordResponse())
  override def findRecord(request: FindRecordRequest, ctx: Metadata): IO[FindRecordResponse] = 
    println(s"findRecord: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(FindRecordResponse())
  override def validateRecord(request: ValidateRecordRequest, ctx: Metadata): IO[ValidateRecordResponse] = 
    println(s"validateRecord: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(ValidateRecordResponse())
  override def invalidateSchema(request: InvalidateSchemaRequest, ctx: Metadata): IO[InvalidateSchemaResponse] = 
    println(s"invalidateSchema: ${ctx.get(Constants.AuthorizationMetadataKey)}")
    IO(InvalidateSchemaResponse())

  def storeRecord(request: Record, metadata: Metadata): IO[Record] = 
    println(s"storeRecord: ${metadata.get(Constants.AuthorizationMetadataKey)}")
    IO(request)
  

object Main extends IOApp.Simple:
  import scala.jdk.CollectionConverters.*

  val recordService: Resource[IO, ServerServiceDefinition] = 
        RecordServiceFs2Grpc.bindServiceResource[IO](new RecordServiceImp, ServerOptions.default )
  val keyService: Resource[IO, ServerServiceDefinition] =
        KeyServiceFs2Grpc.bindServiceResource[IO](new KeyServiceImpl, ServerOptions.default )

  val hookService: Resource[IO, ServerServiceDefinition] =
        HookServiceFs2Grpc.bindServiceResource[IO](new HookServiceImpl, ServerOptions.default )
  val services =
    for 
      r <- recordService.use(s => IO(s))
      k <- keyService.use(s => IO(s))
      h <- hookService.use(s => IO(s))
    yield List(r,k,h)

  def run: IO[Unit] =
    val mySync: Async[IO] = Async[IO]

    val startup: IO[Any] = 
      for
        _ <- IO(println("starting server"))
        s <- services
        _ <- recordService.use{ (service: ServerServiceDefinition) =>
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