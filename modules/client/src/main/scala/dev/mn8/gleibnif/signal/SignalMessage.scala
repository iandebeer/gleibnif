package dev.mn8.gleibnif.signal
import cats.effect.IO
import io.circe.parser.*



//'{"message": "Welcome to D@WNPatrol, the DIDx bot!", "number": "+27659747833", "recipients": [ "+27828870926","+27832582698" ]}
case class Member(name : String, number : String)

case class SignalSendMessage(attachments: List[String],message: String, number: String, recipients: List[String]):
  override def toString(): String =
    s"""SignalSendMessage(message: $message, number: $number, recipients: $recipients, attachments: ${attachments.mkString(",")})"""


case class SignalMessages(messages: List[SignalMessage]):
  override def toString(): String =
    s"""SignalMessages(messages: $messages)"""

case class SignalMessage(val envelope: SignalEnvelope, account: String):
  override def toString(): String =
    s"""SignalMessage(envelope: $envelope, account: $account)"""


case class SignalEnvelope(
  source: String,
  sourceNumber: String,
  sourceUuid: String,
  sourceName: String, 
  sourceDevice: Int,
  timestamp: Long,
 // message: Option[Message],
  dataMessage: Option[SignalDataMessage],
  receiptMessage: Option[SignalReceiptMessage],
  syncMessage: Option[SignalSyncMessage],
  sentMessage: Option[SignalSentMessage]

  ) :
    override def toString(): String =
      s"""SignalEnvelope(source: $source,
      sourceNumber: $sourceNumber, 
      sourceUuid: $sourceUuid, 
      sourceName: $sourceName, 
      sourceDevice: $sourceDevice, 
      timestamp: $timestamp, 
      dataMessage: $dataMessage,
      receiptMessage: $receiptMessage,
      syncMessage: $syncMessage,
      sentMessage: $sentMessage)
      """

sealed trait Message

case class SignalDataMessage(
  timestamp: Long,
  message: String,
  expiresInSeconds: Int,
  viewOnce: Boolean,
  groupInfo: Option[SignalGroupInfo]) extends Message :
    override def toString(): String =
      s"""SignalDataMessage(timestamp: $timestamp, 
       message: $message, 
       expiresInSeconds: $expiresInSeconds, 
       viewOnce: $viewOnce, 
       groupInfo: $groupInfo)"""

case class SignalGroupInfo(
  groupId: String,
  `type`: String // DELIVER, UPDATE, QUIT, REQUEST_INFO, INFO 
): 
  override def toString(): String =
    s"""SignalGroupInfo(groupId: $groupId, 
     type: ${`type`}"""
       

case class SignalReceiptMessage(
  when: Long,
  isDelivery: Boolean,
  isRead: Boolean,
  isViewed: Boolean,
  timestamps: List[Long]) extends Message :
    override def toString(): String =
      s"""SignalReceiptMessage(when: $when, 
       isDelivery: $isDelivery, 
       isRead: $isRead, 
       isViewed: $isViewed, 
       timestamps: $timestamps)"""


case class SignalSyncMessage(
  sentMessage: Option[SignalSentMessage],
  readMessages: Option[List[SignalReadMessage]]
  ) extends Message:
  override def toString(): String =
    s"""SignalSyncMessage(sentMessage: $sentMessage)"""


case class SignalSentMessage(
  destination: String,
  destinationNumber: String,
  destinationUuid: String,
  timestamp: Long,
  message: String,
  expiresInSeconds: Int,
  viewOnce: Boolean,
  groupInfo: Option[SignalGroupInfo]) extends Message:
  override def toString(): String =
    s"""SignalSentMessage(destination: $destination, 
     destinationNumber: $destinationNumber, 
     destinationUuid: $destinationUuid, 
     timestamp: $timestamp, 
     message: $message, 
     expiresInSeconds: $expiresInSeconds, 
     viewOnce: $viewOnce, 
     groupInfo: $groupInfo)"""

case class SignalReadMessage(
  sender: String,
  senderNumber: String,
  senderUuid: String,
  timestamp: Long) extends Message:
    override def toString(): String =
      s"""readMessage(sender: $sender, 
       senderNumber: $senderNumber, 
       senderUuid: $senderUuid, 
       timestamp: $timestamp)"""

case class SignalSimpleMessage(phone:String, name:String, text:String, keywords:List[String] = List()):

  override def toString(): String =
    s"""SignalSimpleMessage(phone: $phone, 
     name: $name, 
     text: $text)
     keywords: ${keywords.mkString(", ")}"""
