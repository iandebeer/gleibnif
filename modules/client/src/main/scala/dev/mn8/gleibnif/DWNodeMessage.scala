package dev.mn8.gleibnif

case class DWNodeMessage(
    messages: List[DWNodeRequestMessage]
):
  override def toString(): String =
    s"""DWNodeMessage(
      |  messages: $messages
      |)""".stripMargin

case class DWNodeRequestMessage(
    recordId: String,
    data: Option[String],
    processing: DWNodeProcessing,
    descriptor: DWNodeDescriptor,
    attestations: Option[List[DWNodeAttestation]],
    authorizations: Option[List[DWNodeAuthorization]]
):
  override def toString(): String =
    s"""DWNodeRequestMessage(
      |  recordId: $recordId,
      |  data: $data,
      |  descriptor: $descriptor,
      |  processing: $processing
      |)""".stripMargin

case class DWNodeDescriptor(
    method: String,
    dataCid: String,
    dataFormat: String
):
  override def toString(): String =
    s"""DWNodeDescriptor(
      |  method: $method,
      |  dataCid: $dataCid,
      |  dataFormat: $dataFormat
      |)""".stripMargin

case class DWNodeProcessing(nonce: String, author: String, recipient: String):
  override def toString(): String =
    s"""DWNodeProcessing(
      |  nonce: $nonce,
      |  author: $author,
      |  recipient: $recipient
       """

case class DWNodeAttestation(
    payload: String,
    signatures: List[DWNodeSignature]
):
  override def toString(): String =
    s"""DWNodeAttestation(
      |  payload: $payload,
      |  signatures: $signatures
      |)""".stripMargin

case class DWNodeAuthorization(
    payload: String,
    signatures: List[DWNodeSignature]
):
  override def toString(): String =
    s"""DWNodeAuthorization(
      |  payload: $payload,
      |  signatures: $signatures
      |)""".stripMargin

case class DWNodeSignature(`protected`: String, signature: String):
  override def toString(): String =
    s"""DWNodeSignature(
      |  protected: ${`protected`},
      |  signature: $signature
      |)""".stripMargin
