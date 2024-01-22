package dev.mn8.gleibnif

import cats.effect.IO
import dev.mn8.gleibnif.didcomm.*
import munit.Assertions.*
import munit.CatsEffectSuite
import munit.FunSuite

class DidExchangeSuite extends CatsEffectSuite {

  test("DID exchange process should complete successfully") {

    // 1. Setup test data
    val inviter = Party(
      "did:example:123456789abcdefghi",
      Role.Inviter,
      State.InvitationSent
    )
    val invitee = Party(
      "did:example:987654321hgfedcba",
      Role.Invitee,
      State.RequestReceived
    )

    // 2. Run the DID exchange process
    // Note: replace `runDidExchange` with the actual method in your code
    val result: IO[Either[Error, State]] =
      new DidExchange().runDidExchange(inviter, invitee)

    // 3. Check the result
    result.flatMap {
      case Right(state) =>
        // The exchange process should result in a completed state
        IO(state).assertEquals(state, State.Completed)
      case Left(error) =>
        fail(s"DID exchange failed with error: $error")
    }
  }
}
