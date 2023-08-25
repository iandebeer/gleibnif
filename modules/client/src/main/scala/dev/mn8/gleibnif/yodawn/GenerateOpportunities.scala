package dev.mn8.gleibnif.yodawn

import scala.util.Random

object OpportunityGenerator extends App {

  case class Opportunity(
      companyName: String,
      title: String,
      description: String = "",
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
  val categories = List("Learning", "Learning", "Task")
  val languages = List("English", "English", "English", "French", "Spanish")
  val locations =
    List("South Africa", "Nigeria", "Kenya", "Morocco", "Ethiopia")

  val keywords = List(
    "Programming",
    "Software Development",
    "Computing basics",
    "Beginner Python",
    "Beginner Java",
    "Microsoft Office",
    "Advanced Scala",
    "Building",
    "Farming",
    "Sales",
    "Leadership",
    "Economics",
    "Climate",
    "Sustainability",
    "Volunteering"
  )

  def getRandomElement[A](list: List[A]): A = list(Random.nextInt(list.length))

  def getRandomDuration: String = s"${Random.nextInt(12) + 1} Hours"

  def getRandomTitle: String = Random.nextString(10)

  def getDescription(op: Opportunity): String =
    f"${op.companyName} is offering a ${op.category} opportunity to learn about ${op.title} in ${op.location}"

  def getRandomOpportunity: Opportunity =
    val op = Opportunity(
      companyName = getRandomElement(companies),
      title = getRandomElement(keywords),
      category = getRandomElement(categories),
      duration = getRandomDuration,
      location = getRandomElement(locations),
      language = getRandomElement(languages)
    )
    op.copy(description = getDescription(op))

  def generateDataset(numberOfOpportunities: Int): List[Opportunity] =
    (1 to numberOfOpportunities).map(_ => getRandomOpportunity).toList

  val opportunities = generateDataset(100)
  opportunities.foreach(println)
}
