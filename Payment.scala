
package dev.mn8.gleibnif
import com.xebia.functional.xef.scala.agents.DefaultSearch
import com.xebia.functional.xef.scala.auto.*
object ChatBot:
  case class Payment(orderId: String, orderDescription: String, orderAmount: String) 
  private def getQuestionAnswer(question: String)(using scope: AIScope): List[String] =
    contextScope(DefaultSearch.search("Weather in Cádiz, Spain")) {
      promptMessage(question)
  }

  @main def runWeather: Unit = ai {
    val question = "Knowing this forecast, what clothes do you recommend I should wear if I live in Cádiz?"
    println(getQuestionAnswer(question).mkString("\n"))
  }.getOrElse(ex => println(ex.getMessage))
