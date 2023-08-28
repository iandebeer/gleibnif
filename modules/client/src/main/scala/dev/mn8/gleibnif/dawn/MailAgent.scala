package dev.mn8.gleibnif.dawn
import cats.effect._
import cats.data.NonEmptyList
import emil._, emil.builder._
import emil.javamail._

object MailAgent extends IOApp.Simple:
  val htmlBody = """
                     |<!DOCTYPE html>
                     |<html>
                     |<head>
                     |    <title>Group Invitation</title>
                     |</head>
                     |<body>
                     |    <h1>Welcome to Our Group!</h1>
                     |    <p>Hello,</p>
                     |    <p>We're excited to have you join our group! To confirm your membership, please click the link below:</p>
                     |    <a href="https://www.example.com/confirm?token=123456">Confirm Membership</a>
                     |    <p>If you didn't request to join this group, you can ignore this email.</p>
                     |    <p>Best,</p>
                     |    <p>The Group Team</p>
                     |</body>
                     |</html>
        """.stripMargin
  // println(s"##################### -> ${getClass.getResource("/").getPath()}")
  val mail = MailBuilder.build(
    From("dawn@didx.co.za"),
    To("ian@didx.co.za"),
    Subject("Invitation to DIDx-Go"),
    CustomHeader(Header("User-Agent", "my-email-client")),
    // TextBody[IO]("Hello, world!"),
    HtmlBody[IO](htmlBody)
  )
  AttachUrl[IO](getClass.getResource("/pass.pkpass"))
    .withFilename("ian.pkpass")
    .withMimeType(MimeType.application("vnd-com.apple.pkpass"))
    .withInlinedContentId("ian.pkpass@42342343")
    .withDisposition(Disposition.Attachment)
  /*  mail.asBuilder.add(
      AttachUrl[IO](getClass.getResource("/qr2.png"),mimeType = MimeType("image", "png"))).build
   */
  val conf = MailConfig(
    "smtp://smtp.gmail.com:587",
    "dawn@didx.co.za",
    "fkifkurnmxwzjzar",
    SSLType.StartTLS
  ) // fkifkurnmxwzjzar - vjccpgdijywmhglk
  IO.println(s"conf = ${conf.urlParts}")
  // val smtpConf = MailConfig("imaps:/" +
  // smtp.gmail.com:465", "dawn@didx.co.za", "4uMj3<TY9UBA7yP=", SSLType.StartTLS)
  val mailer                           = JavaMailEmil[IO]()
  val sendIO: IO[NonEmptyList[String]] = mailer(conf).send(mail)
  def run: IO[Unit] = sendIO.flatMap { result =>
    IO.println(s"Result: $result")
  }
