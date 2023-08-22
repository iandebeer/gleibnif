package dev.mn8.gleibnif.hook

import com.google.protobuf.Message
import dev.mn8.gleibnif.DWNMessage

type DID = String
final case class HookMessage(
    interface: String,
    kind: String,
    action: String,
    name: String,
    owner: DID
) extends DWNMessage:
  enum HookAction:
    case RegisterHookRequest
    case RegisterHookResponse
    case UnregisterHookRequest
    case UnregisterHookResponse
    case HookRequest
    case HookResponse
