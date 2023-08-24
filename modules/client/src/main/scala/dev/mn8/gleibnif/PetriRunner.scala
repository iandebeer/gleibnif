package dev.mn8.gleibnif

import cats.effect.*
import scala.concurrent.ExecutionContext
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router


/** PetriRunner
  *
  * @param interfaceName
  * @param logger
  */

object PetriRunner extends IOApp:
  // given logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]
  given logger: org.log4s.Logger = org.log4s.getLogger
  given ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  def run(args: List[String]): IO[ExitCode] = // starting the server

    val interfaceName = PetriCompiler(args.headOption.getOrElse("purchase"))
    logger.info("This will be logged to both the console and the file.")

    BlazeServerBuilder[IO]
      .withExecutionContext(ec)
      .bindHttp(8080, "localhost")
      .withHttpApp(Router("/" -> (interfaceName.routes)).orNotFound)
      .resource
      .use { _ =>
        IO {
          println("Go to: http://localhost:8080/docs")
          println("Press any key to exit ...")
          scala.io.StdIn.readLine()
        }
      }
      .as(ExitCode.Success)

