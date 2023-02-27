# Signal to DIDComm Protocol

SignalDIDCommProtocol DEFINITION

message = header:content
header = '!'/'@'MOBILE_NO/DID
content = TEXT

e.g. !0828870927:hello there
or   @0828870927:hello there
