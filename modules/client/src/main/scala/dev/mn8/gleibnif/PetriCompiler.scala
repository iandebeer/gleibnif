package dev.mn8.gleibnif

import dev.mn8.gleibnif.ConfigManager.ProtocolConf
import scala.collection.immutable.ListSet
import dev.mn8.castanet.*
import dev.mn8.castanet.{Service => CastanetService}
import org.http4s.HttpRoutes
import org.http4s.server.Router
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import cats.effect.IO
import sttp.client3.{HttpURLConnectionBackend, SttpBackend}
import sttp.model.*
import java.nio.file.{Files, Paths}
import java.util.concurrent.atomic.AtomicReference
import cats.syntax.all.toSemigroupKOps
import cats.data.StateT
import cats.data.State



case class PetriCompiler[F[_]](interfaceName: String)(using
    logger: org.log4s.Logger
):

  val x = this.getClass()

  val protocolConf: ProtocolConf = ConfigManager.protocolConf(interfaceName)

  val pl1: ListSet[String] =
    ListSet.from(protocolConf.weights).flatMap(w => w.actionParams)
  val pl2: ListSet[String] = ListSet
    .from(protocolConf.start.initialParams)
    .flatMap(w => protocolConf.start.initialParams)
  val paramList: ListSet[String] = (pl2 ++ pl1)

  val colourMap: Map[Colour, String] = paramList.zipWithIndex.map {
    case (k, v) => (Colour.fromOrdinal(v), k)
  }.toMap
  val paramMap: Map[String, Colour] =
    colourMap.map(_.swap) // (m => m._2 -> m._1)
  val paramValues = colourMap.map((k, v) => (k -> ""))

  val places: Map[String, Place] = protocolConf.places.map { p =>
    val capacity: Int =
      if (p == protocolConf.start.place) then
        protocolConf.start.initialParams.length
      else
        protocolConf.weights
          .filter(w => w.end == p)
          .foldRight[Int](0)((w, l) => w.actionParams.length)
    (p -> Place(p, capacity))
  }.toMap
  // println(s"places -> $places")

  val transitions: Map[String, Transition] = protocolConf.transitions
    .map(t => (t -> Transition(t, CastanetService(), RPC(t, "", ""))))
    .toMap
  val start = protocolConf.start
  val end = protocolConf.end
  val cpn: ColouredPetriNet =
    val w1: Map[String, ListSet[Weight]] = protocolConf.weights.map { w =>
      (w.end -> ListSet(
        Weight(
          Colour.fromOrdinal(protocolConf.weights.indexOf(w)),
          w.actionParams.length
        )
      ))
    }.toMap
    val inWeights = w1 + (start.place -> ListSet(
      Weight(Colour.fromOrdinal(0), start.initialParams.length)
    ))
    val y = inWeights.map((k: String, v: ListSet[Weight]) =>
      s"${k} -> ${v.map(w => w.colour).mkString(",")}"
    )

    val w2: Map[String, ListSet[Weight]] = protocolConf.weights.map { w =>
      (w.end -> ListSet(
        Weight(
          Colour.fromOrdinal(protocolConf.weights.indexOf(w)),
          w.actionParams.length
        )
      ))
    }.toMap
    val outWeights = w2 // + (end -> ListSet(Weight(Colour.WHITE, 1)))
    val x = outWeights.map((k: String, v: ListSet[Weight]) =>
      s"${k} -> ${v.map(w => w.colour).mkString(",")}"
    )
    val triples: List[PlaceTransitionTriple] = protocolConf.weights.map { w =>
      PlaceTransitionTriple(
        places(w.start),
        inWeights(w.start),
        transitions(w.transition),
        outWeights(w.end),
        places(w.end)
      )
    }
    triples.foldRight(PetriNetBuilder())((t, b) => b.add(t)).build()

  val endpoints: List[
    Endpoint[Unit, List[(String, String)], Unit, Map[String, String], Any]
  ] =
    protocolConf.transitions.map { t =>
      val transition = transitions(t)
      val params: List[String] = protocolConf.weights
        .filter(w => w.transition == t)
        .flatMap(w => w.actionParams)
        .toList

      val e: PublicEndpoint[List[(String, String)], Unit, Map[
        String,
        String
      ], Any] = endpoint.post
        .in(transition.name)
        .in(
          jsonBody[List[(String, String)]]
            .description(
              "List of DWN Record URIs (on IPFS) pertaining to the caller's Instance (DID), providing the requested input data."
            )
            .example(
              params.flatMap(p =>
                Map(
                  p -> "https:<FaaS endpoint> / ipfs:<address> / String value"
                )
              )
            )
        )
        .out(jsonBody[Map[String, String]])
        .description(s"Executes the ${t} transition")

      params.foreach(p => e.in(p))
      e
    }

  val peekEndpoint
      : Endpoint[Unit, String, Unit, (Set[String], Set[String]), Any] =
    endpoint.get
      .in("petrinet")
      .in("peek")
      .in(query[String]("context").description("context to peek"))
      .out(jsonBody[(Set[String], Set[String])])
      .description(
        "Returns the next transition(s) and subsequent place(s) of the Petri Net for the given context"
      )

  val stepEndpoint: Endpoint[Unit, String, Unit, String, Any] =
    endpoint.get
      .in("petrinet")
      .in("step")
      .in(query[String]("context").description("context to step"))
      .out(jsonBody[String])
      .description("Executes one step of the Petri Net for the given context")

  val showEndpoint: Endpoint[Unit, Unit, Unit, Array[Byte], Any] =
    endpoint.get
      .in("petrinet")
      .in("show")
      .out(header(Header.contentType(MediaType.ImagePng)))
      .out(byteArrayBody)
      .description("Renders a png image of the Petri Net")

  def getImage(): IO[Array[Byte]] = IO {
    PetriPrinter(fileName = s"$interfaceName", petriNet = cpn).print()
    Files.readAllBytes(Paths.get(s"${interfaceName}.png"))
  }

  val stateManager: IO[StateManager] = StateManager.create()
  for
    sm <- stateManager
    // _ <- IO(stateManager.updateState("context1", "1")
    _ <- IO(sm.updateState("123", "AAw="))
    _ <- IO(sm.updateState("124", "AAABgA=="))
    _ <- IO(sm.updateState("125", "Afw="))
    _ <- IO(sm.updateState("126", "AHA="))
    _ <- IO(sm.updateState("125", "AAA="))
  yield ()

  def peek(context: String): IO[(Set[String], Set[String])] =
    for
      sm <- stateManager
      state <- sm.getState(context)
      markers = state match
        case Some(m) => Markers(cpn, m)
        case None    => Markers(cpn)
      tuple = cpn.peek(Step(markers))
    yield (tuple._1.map(_.name), tuple._2.map(_.name))
  /*  IO {
    logger.info(s"Peeking Petri Net for context $context")
    val state = stateManager.getState(context)

    val markers = state.match
      case Some(m) => Markers(cpn, m)
      case None    => Markers(cpn)
    val tuple = cpn.peek(Step(markers))
    (tuple._1.map(_.name), tuple._2.map(_.name)) }*/

  def step(context: String): IO[String] =
    for
      sm <- stateManager
      state <- sm.getState(context)
      markers = state match
        case Some(m) => Markers(cpn, m)
        case None    => Markers(cpn)
      steps: State[Step, Unit] =
        for _ <- cpn.step
        yield ()
      step = steps.run(Step(markers)).value._1.markers.serialize
      _ <- sm.updateState(context, step)
    yield s"Stepped Petri Net for context $context"
  /*  IO {
    val state = stateManager.getCurrentState(context)

    val markers = state.match
      case Some(m) => Markers(cpn, m)
      case None    => Markers(cpn)
    val steps: State[Step, Unit] =
      for _ <- cpn.step
      yield ()
    val step = steps.run(Step(markers)).value._1.markers.serialize
    stateManager.updateState(context, step)
    s"Stepped Petri Net for context $context" }*/

  val contextStore =
    new AtomicReference[Map[String, String]](Map.empty[String, String])
  def addStateContext(context: String, state: String): IO[Unit] = IO {
    logger.info(s"Adding state $state to context $context")
    contextStore.updateAndGet(_ + (context -> state))
  }
  def getStateFromContext(context: String): IO[String] = IO {
    logger.info(s"Getting state for context $context")
    contextStore.get().getOrElse(context, "")
  }

  val showEndpointRoutes: HttpRoutes[IO] = Http4sServerInterpreter[IO]()
    .toRoutes(showEndpoint.serverLogicSuccess(_ => getImage()))
  val stepEndpointRoutes: HttpRoutes[IO] = Http4sServerInterpreter[IO]()
    .toRoutes(stepEndpoint.serverLogicSuccess(step _))
  val peekEndpointRoutes: HttpRoutes[IO] = Http4sServerInterpreter[IO]()
    .toRoutes(peekEndpoint.serverLogicSuccess(peek _))

  val interfaceRoutes: List[HttpRoutes[cats.effect.IO]] =
    endpoints.map(e =>
      Http4sServerInterpreter[IO]().toRoutes(
        e.serverLogicSuccess(_ => IO(Map("result" -> "OK")))
      )
    )

  // generating and exposing the documentation in yml
  val swaggerUIRoutes: HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(
      SwaggerInterpreter().fromEndpoints[IO](
        endpoints ++ List(peekEndpoint, stepEndpoint, showEndpoint),
        s"The D@WN ${interfaceName.capitalize} Interface",
        "1.0.0"
      )
    )

  val routes: HttpRoutes[IO] =
    interfaceRoutes.foldRight(swaggerUIRoutes)((r, b) => r <+> b)
      <+> stepEndpointRoutes
      <+> peekEndpointRoutes
      <+> showEndpointRoutes
