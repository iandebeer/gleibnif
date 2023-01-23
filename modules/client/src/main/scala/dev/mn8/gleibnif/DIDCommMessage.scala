package dev.mn8.gleibnif

import java.net.URI

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
