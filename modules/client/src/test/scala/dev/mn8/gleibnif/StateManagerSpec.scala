package dev.mn8.gleibnif
import munit.CatsEffectSuite
import cats.effect.{IO, unsafe}
import cats.syntax.all._

class StateManagerTests extends CatsEffectSuite {

  test("StateManager updates and gets states correctly") {
    val stateManager = new StateManager

    for {
      _ <- IO(stateManager.updateState("context1", "1"))
      state1 <- IO(stateManager.getCurrentState("context1"))
      _ = assertEquals(state1, Some("1"))
      
      _ <- IO(stateManager.updateState("context1", "2"))
      state2 <- IO(stateManager.getCurrentState("context1"))
      _ = assertEquals(state2, Some("2"))

      state3 <- IO(stateManager.getCurrentState("context2"))
      _ = assertEquals(state3, None)
    } yield ()
  }

  test("StateManager handles concurrent updates correctly") {
    val stateManager = new StateManager

    val updates = (1 to 1000).toList.map { i =>
      IO(stateManager.updateState("context1", i.toString()))
    }

    updates.parSequence_.flatMap { _ =>
      IO(stateManager.getCurrentState("context1")).map { finalState =>
        assert(finalState.isDefined, "Final state is defined")
        assert(finalState.get match
            case s if s.toInt < 1 => false
            case s if s.toInt > 1000 => false
            case _ => true,
            "Final state is within the correct range")
        
      }
    }
  }
}