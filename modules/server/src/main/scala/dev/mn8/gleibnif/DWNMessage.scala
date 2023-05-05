package dev.mn8.gleibnif

trait DWNMessage {
  val interface: String
  val kind: String
  val action: String
}
/*
{ // Message
  "descriptor": { // Message Descriptor
    "method": "FeatureDetectionRead"
  }
}
*/
trait Descriptor {
  val method: String
}

/* 
{
  "type": "FeatureDetection",
  "interfaces": { ... }
}
*/
type Interface = String

case class FeatureDetection(`type`: String, interfaces: Map[String, Interface])  {

}