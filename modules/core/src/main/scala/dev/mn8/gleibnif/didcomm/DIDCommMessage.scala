package dev.mn8.gleibnif.didcomm

import java.net.URI
import java.util.UUID


final case class DIDCommMessage(
  id: String,
  `type`: String,
  from: Option[URI],
  to: Option[List[URI]],
  thid: Option[String],
  pthid: Option[String],
  createdTime: Option[String],
  expiresTime: Option[String],
  body: String,
  attachments: Option[List[DIDCommAttachment]]
):
  override def toString(): String = 
    s"""DIDCommMessage(
      |  id: $id,
      |  type: ${`type`},
      |  from: $from,
      |  to: $to,
      |  thid: $thid,
      |  pthid: $pthid,
      |  createdTime: $createdTime,
      |  expiresTime: $expiresTime,
      |  body: $body,
      |  attachments: $attachments
      |)""".stripMargin 

final case class DIDCommBody(
    value: String
):
  override def toString(): String = 
    s"""Body(
      |  value: $value
      |)""".stripMargin

final case class DIDCommAttachment(
    id: String,
    mediaType: Option[String],
    description: Option[String],
    filename: Option[String],
    lastmodTime: Option[String],
    data: Option[DIDCommData],
    byteCount: Option[Int]
):
  override def toString(): String = 
    s"""Attachment(
      |  id: $id,
      |  mediaType: $mediaType,
      |  description: $description,
      |  filename: $filename,
      |  lastmodTime: $lastmodTime,
      |  data: $data,
      |  byteCount: $byteCount
      |)""".stripMargin

final case class DIDCommData(
  jws: Option[String],
  hash: Option[String],
  links: Option[List[URI]],
  base64: Option[String],
  json: Option[String],
):
  override def toString(): String = 
    s"""Data(
      |  jws: $jws,
      |  hash: $hash,
      |  links: $links,
      |  base64: $base64,
      |  json: $json
      |)""".stripMargin


enum Role:
  case Inviter, Invitee

enum State:
  case InvitationSent, RequestReceived, ResponseSent, Completed

case class Party(did: String, role: Role, state: State)

trait DidExchangeMessage:
  def `@id`: UUID
  def `@type`: String

case class Invitation(`@id`: UUID, `@type`: String, label: String, recipientKeys: Seq[String]) extends DidExchangeMessage

case class Request(`@id`: UUID, `@type`: String, label: String, recipientKeys: Seq[String]) extends DidExchangeMessage

case class Response(`@id`: UUID, `@type`: String, label: String, recipientKeys: Seq[String]) extends DidExchangeMessage
