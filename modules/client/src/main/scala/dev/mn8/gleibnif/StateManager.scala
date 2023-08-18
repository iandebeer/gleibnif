package dev.mn8.gleibnif

/* import java.util.concurrent.atomic.AtomicReference
import scala.collection.concurrent.TrieMap

class StateManager {
  private val stateRef: AtomicReference[TrieMap[String, String]] = new AtomicReference(TrieMap.empty)

  def getCurrentState(context: String): Option[String] = {
    val stateMap = stateRef.get()
    stateMap.get(context)
  }

  def updateState(context: String, newState: String): Unit = {
    val oldStateMap = stateRef.get()
    val newStateMap = oldStateMap.updated(context, newState)
    if(!stateRef.compareAndSet(oldStateMap, newStateMap)) {
      updateState(context, newState) // Retry as the state was modified by another thread
    }
  }
}
 */
import cats.effect.IO
import cats.effect.kernel.Ref
import java.util.UUID
import scala.collection.immutable.HashMap
import didcomm.DIDTypes.*

trait State
case class Context(id: String)

class StateManager private (ref: Ref[IO, HashMap[String, String]]) {
  def getState(context: String): IO[Option[String]] =
    ref.get.map(_.get(context))

  def updateState(context: String, state: String): IO[Unit] =
    ref.update(_ + (context -> state))
}

object StateManager {
  def create(): IO[StateManager] =
    Ref.of[IO, HashMap[String, String]](HashMap.empty[String, String]).map(new StateManager(_))
}