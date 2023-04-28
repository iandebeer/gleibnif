package dev.mn8.gleipnif

import TokenTaxonomy._

object Main {
  def main (args: Array[String]): Unit =
    val serialToken =
      """
        |
        |""".stripMargin
    val token = new BaseToken("yip") with Burnable
    token.burn()
    println(token.toString)
    println(Behaviours(1).toStringList())
    printlnUppercase(Behaviours(16).toString())
    println(Behaviours(1048575).toString)
    println(Behaviours("0a"))

}
