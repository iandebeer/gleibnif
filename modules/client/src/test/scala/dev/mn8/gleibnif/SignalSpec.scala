package dev.mn8.gleibnif

import munit.FunSuite
import dev.mn8.gleibnif.signal.*
import dev.mn8.gleibnif.openai.OpenAIAgent
import cats.effect.IO
import cats.effect.unsafe.implicits._
import dev.mn8.gleibnif.signal.SignalSimpleMessage
import io.circe._, io.circe.parser._, io.circe.syntax._

import dev.mn8.gleibnif.signal.SignalMessageCodec.memberDecoder


class SignalSpec extends FunSuite {
 /*  val openAIAgent = OpenAIAgent()
  test("listen for messages") {
    val l: IO[List[SignalSimpleMessage]] = SignalBot().receive()
    l.flatTap(m => IO(println(s"Received messages: $m"))).unsafeRunSync()
    
  }
 */
 /*  test("send message") {
    val l: IO[Unit] = SignalBot().send(SignalSendMessage(List[String](),"Hello from D@WN Patrol", "+27659747833", List("+27828870926")))
    l.flatTap(m => IO(println(s"Sent messages"))).unsafeRunSync()
  
 //   openAIAgent.getConf()
  }   */
 /*  test("extract keywords") {
    val l = openAIAgent.extractKeywords(SignalSimpleMessage("","","Remember to renew your motor license before Friday 24 February 2023", List[String]())) 
     l.flatTap(m => IO(println(s"Received messages: $m"))).unsafeRunSync()
  }  */ 
  test("get add command") {
    val x = """@admin|add|{"name": "Ian de Beer",  "number": "1234567"}"""
    val y = x.split("\\|")(2)
    println(s"line1: $y")
    val z = parse(y).match 
      case Left(error) => 
        println(s"Error: $error")
        Member("","")
      case Right(conf) => conf.as[Member].getOrElse(Member("",""))
    println(z)
  }
}
