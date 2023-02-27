package dev.mn8.gleibnif

import munit.FunSuite
import dev.mn8.gleibnif.signal.*
import dev.mn8.gleibnif.openai.OpenAIAgent
import cats.effect.IO
import cats.effect.unsafe.implicits._
import dev.mn8.gleibnif.signal.SignalSimpleMessage


class SignalSpec extends FunSuite {
  val openAIAgent = OpenAIAgent()
  test("listen for messages") {
    val l: IO[List[SignalSimpleMessage]] = SignalBot().receive()
    l.flatTap(m => IO(println(s"Received messages: $m"))).unsafeRunSync()
    
  }

  test("extract conf") {
 //   openAIAgent.getConf()
  }  
  test("extract keywords") {
    val l = openAIAgent.extractKeywords(SignalSimpleMessage("","","Remember to renew your motor license before Friday 24 February 2023", List[String]())) 
     l.flatTap(m => IO(println(s"Received messages: $m"))).unsafeRunSync()
  }  
    
}
