package dev.mn8.gleipnif

import scodec.bits._
import scala.quoted.*
import scala.math._

object TokenTaxonomy:
  enum Behaviour:
    case Attestable extends Behaviour
    val script: String = ""
    case Burnable           extends Behaviour
    case Compliant          extends Behaviour
    case Credible           extends Behaviour
    case Delegable          extends Behaviour
    case Divisible          extends Behaviour
    case Encumberable       extends Behaviour
    case Fabricteable       extends Behaviour
    case Holdable           extends Behaviour
    case Issuable           extends Behaviour
    case Logable            extends Behaviour
    case Mintable           extends Behaviour
    case Offsetable         extends Behaviour
    case Overdraftable      extends Behaviour
    case Pauseable          extends Behaviour
    case Processable        extends Behaviour
    case Redeemable         extends Behaviour
    case Revocable          extends Behaviour
    case Roles              extends Behaviour
    case Singleton          extends Behaviour
    case NonTransferable    extends Behaviour
    case UniqueTransferable extends Behaviour

  object Behaviour:
    def evaluate(): Boolean =
      true

  def printlnUppercaseImpl(str: Expr[String])(using q: Quotes): Expr[Unit] =
    val expr: Expr[String] = '{ $str.toUpperCase }
    '{ println($expr) }

  inline def printlnUppercase(str: String): Unit = ${
    printlnUppercaseImpl('str)
  }

  def instantiateToken(behaviours: Behaviours): BaseToken = ???

  case class Behaviours(list: List[Behaviour]):
    def toStringList(): List[String] = list.map(b => b.toString())
    override def toString            = toStringList().mkString(",")
  object Behaviours:
    def apply(bits: Int) =
      new Behaviours(asList(bits))
    def apply(hex: String) =
      new Behaviours(asList(Integer.parseInt(hex, 16)))
    private def asList(bits: Int) =
      val bv: BitVector = BitVector.fromInt(bits).reverse
      Behaviour.values.foldLeft(List[Behaviour]()) { (l, b) =>
        if (bv(b.ordinal))
        then l.:+(b)
        else l
      }
    // def is(behaviour:Behaviour, )
    // List()

  trait Token:
    val name: String
  trait TokenType

  trait Attestable extends TokenType:
    def attest(contract: String): Boolean
  trait Fungible    extends TokenType
  trait NonFungible extends TokenType
  trait TokenUnit   extends Token
  trait Fractional  extends TokenUnit
  trait Whole       extends TokenUnit
  trait TokenValue  extends Token
  trait Intrinsic   extends TokenValue
  trait Reference   extends TokenValue
  // trait TokenRepresentation(name:String) extends Token(name: String)
  trait Burnable extends Token:
    def burn() = println(s"burn $name")
  // trait Unique extends TokenRepresentation
  trait TokenSupply                           extends Token
  trait Fixed                                 extends TokenSupply
  trait CappedVariable                        extends TokenSupply
  trait Gated                                 extends TokenSupply
  trait Infinite                              extends TokenSupply
  trait TokenTemplate                         extends Token
  trait Single                                extends TokenTemplate
  trait Hybrid                                extends TokenTemplate
  case class BaseToken(name: String = "blah") extends Token
