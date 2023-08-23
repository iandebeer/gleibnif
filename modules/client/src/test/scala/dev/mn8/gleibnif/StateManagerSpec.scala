package dev.mn8.gleibnif
import munit.CatsEffectSuite
import cats.effect.{IO, unsafe, Concurrent}
import cats.syntax.all._
import cats.implicits._
import java.util.UUID
import cats.effect.implicits.concurrentParSequenceOps
import cats.syntax.all._
import cats.Traverse
import scala.concurrent.duration._

class StateManagerTests extends CatsEffectSuite {

  case class TestState(i: Int) extends State

  test("StateManager updates and gets states correctly") {
    val stateManager = StateManager.create()

    for {
      sm <- stateManager
      _ <- sm.updateState("context1", "1")
      state1 <- sm.getState("context1")
      _ <- IO.println(s"state1 = $state1")

      _ = assertIO(IO(state1), Some("1"))

      _ <- sm.updateState("context1", "2")
      state2 <- sm.getState("context1")
      _ <- IO.println(s"state2 = $state2")
      _ = assertIO(IO(state2), Some("2"))

      state3 <- sm.getState("context2")
      _ = assertIO(IO(state3), None)
    } yield ()
  }

  test("StateManager should correctly handle concurrent updates") {

    val stateManager = StateManager.create()
    def update(sm: StateManager, i: Int): IO[Unit] =
      for
        _ <- IO.println(s"update $i")
        _ <- sm.updateState("1", i.toString())
      yield ()

      // Run the updates concurrently
    def updates(sm: StateManager) =
      (1 to 100).toList.map(i => update(sm, i)).parSequenceN(100)

    // updates.parSequenceN(10)
    val state = for
      sm <- stateManager
      _ <- updates(sm)
      s <- sm.getState("1")
      _ <- IO.println(
        s"after concurrent updates state = ${s.getOrElse("None")}"
      )
    yield (s)
    IO.println(
      s"after concurrent updates state = ${state.map(s => s.getOrElse("None"))}"
    )

    state.map(s => assert(s.getOrElse("0").toInt > 0))
    // stateManager.flatMap(sm => sm.getState("1").map(s => assert(s.getOrElse("-1").toInt > 0)))
    // Retrieve the state
  }

  def someIOOperation(i: Int): IO[Int] = IO.pure(i * 2)

  test("parSequenceN test") {
    val operations = (1 to 100).toList.map(someIOOperation)
    val result = operations.parSequenceN(10)
    for
      r <- result
      _ = IO.println(s"list = $r")
      _ = println(s"list = $r")
    yield () // limiting the concurrency to 10 tasks at a time
    result.assertEquals((1 to 100).map(_ * 2).toList)
    result.map(r => assertEquals(r, (1 to 100).map(_ * 2).toList))
  }
}
