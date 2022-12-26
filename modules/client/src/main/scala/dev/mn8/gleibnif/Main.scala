package dev.mn8.gleipnif

import cats.effect.*
import cats.effect.std.Dispatcher
import fs2.*
import _root_.io.grpc.*
import fs2.grpc.syntax.all.*
import java.util.concurrent.Executor
import Constants.*

import scala.concurrent.ExecutionContext.Implicits.global
import fs2.grpc.client.ClientOptions
import _root_.io.grpc.ClientInterceptor

import cats.effect.IO
import org.ergoplatform.flow.spec.flowspec.FlowSpec.{Parameter => Param}

/* import org.ergoplatform.flow.spec.flowspec.PetrinetFs2Grpc
import org.ergoplatform.flow.spec.flowspec.Wallet.Box
import org.ergoplatform.flow.spec.flowspec.Wallet.Box.ErgCondition
import org.ergoplatform.flow.spec.flowspec.Transaction */
import org.ergoplatform.flow.spec.flowspec.Wallet
import org.ergoplatform.flow.spec.flowspec.Wallet.Box.ErgCondition
import org.ergoplatform.flow.spec.flowspec.Transaction
import org.ergoplatform.flow.spec.flowspec.Transaction.InputArrow
import org.ergoplatform.flow.spec.flowspec.Transaction.SpendingPath
import org.ergoplatform.flow.spec.flowspec.PetrinetFs2Grpc
import org.ergoplatform.flow.spec.flowspec.FlowSpec

object Main extends IOApp:

  case class JwtCredentials() extends CallCredentials:
    override def thisUsesUnstableApi(): Unit = {}
    override def applyRequestMetadata(
        requestInfo: CallCredentials.RequestInfo,
        appExecutor: Executor,
        applier: CallCredentials.MetadataApplier
    ): Unit =
      val headers = new Metadata()
      headers.put[String](AuthorizationMetadataKey, "test")
      applier.apply(headers)

  case class KeycloakInterceptor(s: String) extends ClientInterceptor:
    override def interceptCall[Req, Res](
        methodDescriptor: MethodDescriptor[Req, Res],
        callOptions: CallOptions,
        channel: Channel
    ) =
      println("hello from the client")
      channel.newCall[Req, Res](methodDescriptor, callOptions.withCallCredentials(JwtCredentials()))

  val managedChannelStream: Stream[IO, ManagedChannel] =
    ManagedChannelBuilder
      .forAddress("127.0.0.1", 9999)
      .usePlaintext()
      .intercept(KeycloakInterceptor("hi"))
      .stream[IO]

  def defineSpec() =
    // The game contract is created by the second player using the funds from the createGameTransaction
    // The output can be spent by the second player after the end of the game, if the first player fails to provide its secret and answer for the withdrawal
    // At any time, the winner can withdraw the funds providing the answer and the secret of the first player
    val gameScript = s""" 
                        | { // Get inputs from the createGameTransaction, this is the last box in the input list
                        |   val p2Choice = INPUTS(INPUTS.size-1).R4[Byte].get
                        |   val p1AnswerHash = INPUTS(INPUTS.size-1).R5[Coll[Byte]].get
                        |   val player1Pk = INPUTS(INPUTS.size-1).R6[SigmaProp].get
                        |   val partyPrice = INPUTS(INPUTS.size-1).R7[Long].get
                        |   val game_end = INPUTS(INPUTS.size-1).R8[Int].get
                        |   
                        |   // Get the outputs register
                        |   val p1Choice = OUTPUTS(0).R4[Byte].get
                        |   val p1Secret = OUTPUTS(0).R5[Coll[Byte]].get
                        |   
                        |   // Compute the winner (the check of the correctness of the winner answer is done later)
                        |   val p1win = ( p2Choice != p1Choice )
                        |   
                        |   sigmaProp (
                        |      // After the end of the game the second player wins by default
                        |      // This prevents the first player to block to game by not releasing its answer and secret
                        |     (player2Pk && HEIGHT > game_end) || 
                        |       allOf(Coll(
                        |         // The hash of the first player answer must match
                        |         blake2b256(p1Secret ++ Coll(p1Choice)) == p1AnswerHash,
                        |         // The winner can withdraw
                        |         (player1Pk && p1win )|| (player2Pk && ( p1win == false ))
                        |       ))
                        |   )
                        |  }
 """.stripMargin

    // The create game contract is created by the first player to engage the game
    // It allows to cancel the game and get a refund after the end of the game
    // At any time the funds can be spent by the second player if:
    //   The funds are protected by the game script
    //   The output value is more than twice the party price
    //   The R5 register contains the hash the the answer of the first player
    //   The R6 register contains public key of the first player
    //   The party price and game_end are unchanged from the initial contract
    val createGameScript = s""" 
      {
        val gameScriptHash = SELF.R4[Coll[Byte]].get
        val p1AnswerHash = SELF.R5[Coll[Byte]].get

        sigmaProp (
          (player1Pk && HEIGHT > game_end) ||
              allOf(Coll(
                  player2Pk,
                  blake2b256(OUTPUTS(0).propositionBytes) == gameScriptHash,
                  OUTPUTS(0).value >= 2 * partyPrice,
                  OUTPUTS(0).R5[Coll[Byte]].get == p1AnswerHash,
                  OUTPUTS(0).R6[SigmaProp].get == player1Pk,
                  OUTPUTS(0).R7[Long].get == partyPrice,
                  OUTPUTS(0).R8[Int].get == game_end
                ))
        )
      }
    """.stripMargin

    val name: String = "Coin Flip Game"
    val parameters = Seq(
      Param("playPrice", "Long"),
      Param("playPriceMinTxFee", "Long"),
      Param("playerFunds", "Long"),
      Param("gameDuration", "Long"),
      Param("playNumber", "Long")
    )
    val wallets: Seq[Wallet] = Seq(
      Wallet(
        "player1",
        "ergo script",
        Seq(Wallet.Box("standard", Option(ErgCondition("this", "script"))))
      ),
      Wallet(
        "player2",
        "ergo script",
        Seq(Wallet.Box("standard", Option(ErgCondition("this", "script"))))
      )
    )
    val transaction = Seq(
      Transaction(
        name = "createGame",
        inputs = Seq(InputArrow("p2Choice", "fromBox", Some(SpendingPath("action", "Condition"))))
      )
    )

    //val boxes: Seq[Wallet.Box] = Seq(new Wallet.Box("",None))

    FlowSpec(name, parameters, wallets, transaction)

  override def run(args: List[String]): IO[ExitCode] = {

    val flow: FlowSpec = defineSpec()

    for {
      dispatcher     <- Stream.resource(Dispatcher[IO])
      managedChannel <- managedChannelStream
      flowStub = PetrinetFs2Grpc.stub[IO](dispatcher, managedChannel, ClientOptions.default)
      _ <- Stream.eval(
        for {
          response <- flowStub.addFlow(flow, new Metadata())
          message  <- IO(response.message)
          _        <- IO.println(message)
        } yield ()
      )

    } yield ()
  }.compile.drain.as(ExitCode.Success)
