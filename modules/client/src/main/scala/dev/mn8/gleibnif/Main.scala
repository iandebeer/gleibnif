package dev.mn8.gleipnif

import cats.effect.*
import cats.effect.std.Dispatcher
import cats.Monad
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.data.{EitherT, WriterT}
import cats.implicits._
import cats.syntax.all._
import cats.FunctorFilter.ops.toAllFunctorFilterOps
import cats.syntax.traverse._
import fs2.Compiler.Target.forSync


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import Constants.*

import javax.security.auth.login.AppConfigurationEntry
import java.net.URL
import java.util.concurrent.Executor

import pureconfig.*
import pureconfig.generic.derivation.default.*

import io.circe.parser.*
import io.circe._, io.circe.parser._, io.circe.syntax._
import fs2.Stream

import dev.mn8.gleibnif.signal.*
import SignalMessageCodec.memberDecoder
import dev.mn8.gleibnif.didops.RegistryServiceClient
import dev.mn8.gleibnif.dawn.DWNClient
import dev.mn8.gleibnif.passkit.PasskitAgent
import dev.mn8.gleibnif.openai.OpenAIAgent
import sttp.client3.ResponseException
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import dev.mn8.gleibnif.DIDDoc
import dev.mn8.gleibnif.Service
import java.net.URI
import dev.mn8.gleibnif.didops.RegistryRequest
import dev.mn8.gleibnif.didops.RegistryResponseCodec.encodeRegistryRequest
import dev.mn8.gleibnif.ServiceEndpointNodes
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.client3.SttpBackend

import  dev.mn8.castanet.*
import dev.mn8.castanet.{Service => CastanetService}
import scala.collection.immutable.ListSet

class Services()(using logger: Logger[IO]):
  type ErrorOr[A] = EitherT[IO, Exception, A]
 // type LoggedEitherT[F[_], E, A] = WriterT[EitherT[F, E, *], Logger[F], A]

  
  def info[T](value: T)(using logger: Logger[IO]): IO[Unit] =
    println(s"Main: $value")
    logger.info(s"$value")
  
  def err[T](value: T)(using logger: Logger[IO]): IO[Unit] =
    println(s"Main: $value")
    logger.error(s"$value") 

  case class AppConf(
      redisUrl: URL,
      redisTimeout: Int,
      ipfsClusterUrl: URL,
      universalResolverUrl: URL,
      universalResolverTimeout: Int,
      ipfsClusterTimeout: Int,
      pollingInterval: Int,
      prismUrl: URL,
      prismToken: String,
      dawnUrl: URL,
      dawnControllerDID: String,
      dawnServiceUrls: Set[URI],
      dawnWelcomeMessage: String,
      protocolsEnabled: List[String],
  ) derives ConfigReader:
    override def toString(): String =
      s"""
      |redisUrl: $redisUrl
      |redisTimeout: $redisTimeout
      |ipfsClusterUrl: $ipfsClusterUrl
      |universalResolverUrl: $universalResolverUrl
      |universalResolverTimeout: $universalResolverTimeout
      |ipfsClusterTimeout: $ipfsClusterTimeout
      |pollingInterval: $pollingInterval
      |prismUrl: $prismUrl
      |prismToken: $prismToken
      |dawnUrl: $dawnUrl
      |dawnControllerDID: $dawnControllerDID
      |dawnServiceUrls: $dawnServiceUrls
      |dawnWelcomeMessage: $dawnWelcomeMessage
      |protocolsEnabled: $protocolsEnabled
      |""".stripMargin

  case class RegistryConf(
      registrarUrl: URL,
      registrarTimeout: Int,
      apiKey: String,
      didMethod: String
  ) derives ConfigReader:
    override def toString(): String =
      s"""
      |registryUrl: $registrarUrl
      |registryTimeout: $registrarTimeout
      |didMethod: $didMethod
      |""".stripMargin

  def getConf() =
    val appConf: AppConf =
      ConfigSource.default.at("app-conf").load[AppConf] match
        case Left(error) =>
          err(s"Error: $error")
          AppConf(
            new URL("http://localhost:8080"),
            0,
            new URL("http://localhost:8080"),
            new URL("http://localhost:8080"),
            0,
            0,
            0,
            new URL("http://localhost:8080"),
            "",
            new URL("http://localhost:8080"),
            "",
            Set(new URI("http://localhost:8080")),
            "WELCOME TO DAWN",
            List()
          )

        case Right(conf) => conf
    appConf
  def getRegistryConf() =
    val registryConf: RegistryConf =
      ConfigSource.default.at("registry-conf").load[RegistryConf] match
        case Left(error) =>
          err(s"Error: $error")
          RegistryConf(new URL("http://localhost:8080"), 0, "", "indy")
        case Right(conf) => conf
    registryConf
  def getSignalConf() =
    val signalConf: SignalConf =
      ConfigSource.default.at("signal-conf").load[SignalConf] match
        case Left(error) =>
          err(s"Error: $error")
          SignalConf(new URL("http://localhost:8080"), 0, "")
        case Right(conf) => conf
    signalConf

  /*
  proto {
    places = ["start", "shopping-list", "payment", "delivery", "end"]
    transitions = ["order", "pay", "deliver"]
    start = { place = "start", weight = 2, initial-params = ["did","location"]}
    end = "end"
    weights = [
      {start = "start", transition = "order", end = "shopping-order", action = "create-order", action-params = ["order-date","order-id", "order-name", "order-description", "order-amount"]},
      {start = "shopping-list", transition = "pay", end = "payment", action = "pay-order", action-params = ["order-id", "order-amount"]},
      {start = "payment", transition = "deliver", end = "delivery", action = "receive-order", action-params = ["order-id", "location"]},
      {start = "delivery", transition = "deliver", end = "end",  action = "complete-order", action-params = ["order-id", "order-amount"]},
    ]
  */

  case class WeightConf(
      start: String,
      transition: String,
      end: String,
      action: String,
      actionParams: List[String],
  ) derives ConfigReader:
    override def toString(): String =
      s"""
      |start: $start
      |transition: $transition
      |end: $end
      |action: $action
      |actionParams: $actionParams
      |""".stripMargin

  case class StartConf(
      place: String,
      weight: Int,
      initialParams: List[String]
  ) derives ConfigReader:
    override def toString(): String =
      s"""
      |place: $place
      |weight: $weight
      |initialParams: $initialParams
      |""".stripMargin

  case class ProtocolConf(
      places: List[String],
      transitions: List[String],
      start: StartConf,
      end: String,
      weights: List[WeightConf],
  ) derives ConfigReader:
    override def toString(): String =
      s"""
      |places: $places
      |transitions: $transitions
      |start: $start
      |end: $end
      |weights: $weights
      |""".stripMargin
  case class SignalConf(
      signalUrl: URL,
      signalTimeout: Int,
      signalPhone: String,
  ) derives ConfigReader:
    override def toString(): String =
      s"""
      |signalUrl: $signalUrl
      |signalTimeout: $signalTimeout
      |signalPhone: $signalPhone
      |""".stripMargin

  val appConf = getConf()
  val registryConf = getRegistryConf()
  val signalConf = getSignalConf()
  val registryClient = RegistryServiceClient(
    registryConf.registrarUrl.toString(),
    registryConf.apiKey
  )
  def buildPetriNet(p: String):ColouredPetriNet =
    val protocolConf: ProtocolConf =
      ConfigSource.default.at(s"$p-proto").load[ProtocolConf] match
        case Left(error) =>
          err(s"Error: $error")
          ProtocolConf(List(), List(), StartConf("", 0, List()), "", List())
        case Right(conf) => conf
    val places:Map[String, Place] = protocolConf.places.map{ p => 
      val capacity: Int= if (p == protocolConf.start.place) then 
          protocolConf.start.initialParams.length 
        else  
          protocolConf.weights.filter(w => w.start == p).foldRight[Int](0)((w, l) => w.actionParams.length)
      (p -> Place(p,capacity))}.toMap
    println(s"places -> $places")

    val transitions: Map[String,Transition] = protocolConf.transitions.map(t => (t ->Transition(t,CastanetService(),RPC(t,"","")))).toMap
    val start = protocolConf.start
    val end = protocolConf.end
    val w1: Map[String,ListSet[Weight]] = protocolConf.weights.map{ w => 
      (w.end -> ListSet(Weight(Colour.fromOrdinal(protocolConf.weights.indexOf(w)),w.actionParams.length)))
    }.toMap
    val inWeights = w1 + (start.place -> ListSet(Weight(Colour.fromOrdinal(0),start.initialParams.length)))
    println(s"in -> $inWeights")
    val w2: Map[String,ListSet[Weight]] = protocolConf.weights.map{ w => 
      (w.start -> ListSet(Weight(Colour.fromOrdinal(protocolConf.weights.indexOf(w)),w.actionParams.length)))
    }.toMap
    val outWeights = w2 + (end -> ListSet(Weight(Colour.WHITE,0)))
    println(s"out -> $outWeights")

    val triples: List[PlaceTransitionTriple] = protocolConf.weights.map{ w => 
      PlaceTransitionTriple(places(w.start),
      inWeights(w.start),
      transitions(w.transition),
      outWeights(w.end), 
      places(w.end))
    }
    // val ptt2: List[PlaceTransitionTriple] = protocolConf.weights.find(w => w.end == protocolConf.end).map{ w => 
    //   triples :+ PlaceTransitionTriple(places(w.start),
    //   inWeights(w.start),
    //   transitions(w.transition),
    //   outWeights(w.end), 
    //   places(w.end))
    // }.getOrElse(triples)
    
    
    //val ptt2 = ptt1++(ptt2)
    triples.foldRight(PetriNetBuilder())((t, b) => b.add(t)).build()

  // val protocols = appConf.protocolsEnabled.map(p => Map(
  //   p -> buildPetriNet(p)
  // ))
 
 // val protoNet =  PetriNetBuilder().add(ptt1).add(ptt2).add(ptt3).add(ptt4).add(ptt5).build()

  def callServices(backend:  SttpBackend[cats.effect.IO, Any] ): IO[Either[Exception, List[String]]] =
    val signalBot = SignalBot(backend)
    val openAIAgent = OpenAIAgent(backend)
    val message = EitherT{signalBot.receive().flatTap(m => logger.info(s"Processing input: ${m}"))}
    
    def processKeywords(k: SignalSimpleMessage): EitherT[IO, Exception, String] = {
      EitherT(k match 
        case  m: SignalSimpleMessage if m.text.toLowerCase().contains("https://maps.google.com") => 
          ???//openAIAgent.keywords(m.text.split("\\|")(2))
          
          )
      EitherT(signalBot.send(SignalSendMessage(List[String](), s"I have extracted the following keywords: ${k.keywords.mkString(",")}", signalConf.signalPhone, List(k.phone))))
     }

    (for
      mt <- message.map(
            _.filter(_.text.length > 0)
              .partition(_.text.toLowerCase().startsWith("@admin|add"))
          ) 
     
      adminMsg <- mt._1
        .map(m =>
          val member = decode[Member](m.text.split("\\|")(2)).getOrElse(Member("", ""))
          val doc = DIDDoc(
            did = "",
            controller = Some(s"${appConf.dawnControllerDID}"),
            alsoKnownAs = Some(Set(s"tel:${member.number}.};name=${member.name}")),
            services = Some(
              Set(
                Service(
                  id = new URI("#dwn"),
                  `type` = Set("DecentralizedWebNode"),
                  serviceEndpoint = Set(
                    ServiceEndpointNodes(
                      nodes = appConf.dawnServiceUrls
                    )
                  )
                )
              )
            )
          )
          val reg = RegistryRequest(doc)
          val document = reg.asJson.spaces2
          for
            did <- EitherT(//(backend.use { b =>
              registryClient.createDID(registryConf.didMethod, document, backend))
            member <- EitherT.fromEither(decode[Member](m.text.split("\\|")(2)))
            pass <- PasskitAgent(member.name, did, appConf.dawnUrl).signPass()
            r <- EitherT(//backend.use { b =>
              signalBot.send(
                SignalSendMessage(
                  List[String](
                    s"data:application/vnd.apple.pkpass;filename=did.pkpass;base64,${pass}"
                  ),
                  s"${member.name}, ${appConf.dawnWelcomeMessage}",
                  s"${signalConf.signalPhone}",
                  List[String](member.number)
                )
              ))
             _ <- EitherT.liftF(logger.info(s"sent badge with $did to : ${member.name} "))
           // })
          yield r
        )
        .sequence

      keywords: List[SignalSimpleMessage] <- mt._2.map((m: SignalSimpleMessage) => openAIAgent.extractKeywords(m)).sequence
      s: List[String] <- 
        keywords.traverse(processKeywords)
    yield s).value

object Main extends IOApp.Simple:   
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val pollingInterval: FiniteDuration = 10.seconds
  val services = new Services()
  val run = Dispatcher[IO].use { dispatcher =>
      val pollingStream = for {
        backend: SttpBackend[cats.effect.IO, Any] <- Stream.resource(AsyncHttpClientCatsBackend.resource[IO]())
        _ <- Stream.fixedRate[IO](10.seconds) // Poll every 10 seconds
        data <- Stream.eval(services.callServices(backend))
       // _ <- Stream.sleep[IO](10.millis) // Explicitly yield by sleeping for a short duration

      } yield data

      val pollingWithLogging = pollingStream
       // .evalTap(data => IO(println(s"Received data: $data")))
        .compile
        .drain

      for {
        fiber <- pollingWithLogging.start
        _ <- fiber.join
      } yield ()
    }