package dev.mn8.gleibnif

import java.util.concurrent.atomic.AtomicReference
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

