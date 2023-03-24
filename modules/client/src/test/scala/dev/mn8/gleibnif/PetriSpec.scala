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

import scodec.bits.*

import scala.collection.immutable.ListSet
import scala.io.Source
import scala.quoted.*

class PetriSpec extends FunSuite {
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
                        |      // This prevents the first player to block to game by not relealing its answer and secret
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
 


  test("build petri net") {

    import Arc._

    val start: Place = Place("start", 1)
    val left: Place  = Place("left", 3)
    val right: Place = Place("right", 1)
    val joint: Place = Place("joint", 3)
    val end: Place   = Place("end", 1)
    val s1 = Service(
      "dev.mn8.castanet",
      "HelloFs2Grpc",
      List[RPC](RPC(name = "sayHello", input = "", output = ""))
    )
    val r1                    = s1.rpcs.head
    val splitter: Transition  = Transition("splitter", s1, r1)
    val joiner: Transition    = Transition("joiner", s1, r1)
    val continuer: Transition = Transition("continuer", s1, r1)

    val w1   = Weight(Colour.LIGHT_BLUE, 1)
    val w2   = Weight(Colour.LIGHT_BLUE, 1)
    val w3   = Weight(Colour.LIGHT_BLUE, 1)
    val w4   = Weight(Colour.LIGHT_BLUE, 2)
    val w5   = Weight(Colour.LIGHT_BLUE, 1)
    val w6   = Weight(Colour.LIGHT_BLUE, 1)
    val w7   = Weight(Colour.LIGHT_BLUE, 3)
    val w8   = Weight(Colour.LIGHT_BLUE, 1)
    val ptt1 = PlaceTransitionTriple(start, ListSet(w1), splitter, ListSet(w2), left)
    val ptt2 = PlaceTransitionTriple(start, ListSet(w2), splitter, ListSet(w3), right)
    val ptt3 = PlaceTransitionTriple(left, ListSet(w4), joiner, ListSet(w6), joint)
    val ptt4 = PlaceTransitionTriple(right, ListSet(w5), joiner, ListSet(w6), joint)
    val ptt5 = PlaceTransitionTriple(joint, ListSet(w7), continuer, ListSet(w8), end)

    val pn = PetriNetBuilder().add(ptt1).add(ptt2).add(ptt3).add(ptt4).add(ptt5).build()

    val places = pn.elements.values.collect { case p: Place =>
      p
    }
    val dimensions = (places.size, places.maxBy(p => p.capacity).capacity)
    println(dimensions)

    val m1 = Markers(pn)
    println(s"${m1}\n${m1.toStateVector}")

    val m2 = m1.setMarker(Marker(start.id, bin"1"))
    println(s"${m2}\n${m2.toStateVector}")

    val m3 = m2.setMarker(Marker(left.id, bin"1")).setMarker(Marker(joint.id, bin"11"))
    println(s"${m3}\n${m3.toStateVector}")

    val m4 = Markers(pn, m3.toStateVector)
    println(s"${m4}\n${m4.toStateVector} \n${m4.serialize}")

    val m5 = Markers(pn, m4.serialize)
    println(s"${m5}\n${m5.toStateVector} \n${m5.serialize}")
    PetriPrinter(fileName = "petrinet1", petriNet = pn).print(Option(m3))
    val steps: State[Step, Unit] =
      for
        p1 <- pn.step
        p2 <- pn.step
        p3 <- pn.step
      yield (
        PetriPrinter(fileName = "petrinet2", petriNet = pn).print(Option(p1)),
        PetriPrinter(fileName = "petrinet3", petriNet = pn).print(Option(p2)),
        PetriPrinter(fileName = "petrinet4", petriNet = pn).print(Option(p3))
      )
    steps.run(Step(m3, true, 1)).value

  }
}
