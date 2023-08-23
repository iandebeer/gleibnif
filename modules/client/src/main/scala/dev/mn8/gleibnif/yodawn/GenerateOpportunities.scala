package dev.mn8.gleibnif.yodawn

import scala.util.Random

object OpportunityGenerator extends App {

  case class Opportunity(
      companyName: String,
      title: String,
      description: String,
      category: String,
      duration: String,
      location: String,
      language: String
  )

  val companies = List(
    "Generation Unlimited",
    "Cisco",
    "Atingi",
    "Goodwall",
    "UNICEF South Africa"
  )
  val categories = List("Learning", "Task")
  val languages = List("English", "French", "Spanish")
  val locations = List("Worldwide", "South Africa", "Nigeria")

  def getRandomElement[A](list: List[A]): A = list(Random.nextInt(list.length))

  def getRandomDuration: String = s"${Random.nextInt(100) + 1} Hours"

  def getRandomTitle: String = Random.nextString(10)
  def getRandomDescription: String = Random.nextString(10)

  def getRandomOpportunity: Opportunity = Opportunity(
    companyName = getRandomElement(companies),
    title = getRandomTitle,
    description = getRandomDescription,
    category = getRandomElement(categories),
    duration = getRandomDuration,
    location = getRandomElement(locations),
    language = getRandomElement(languages)
  )

  def generateDataset(numberOfOpportunities: Int): List[Opportunity] =
    (1 to numberOfOpportunities).map(_ => getRandomOpportunity).toList

  val opportunities = generateDataset(10)
  opportunities.foreach(println)
}
