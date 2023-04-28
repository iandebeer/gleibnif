package dev.mn8.gleibnif
import cats.effect.IO
import cats.effect.Resource
import dev.mn8.gleibnif.passkit.PasskitAgent
import munit.*
import net.glxn.qrgen.javase.QRCode
import play.api.libs.json.JsonNaming.PascalCase

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import cats.effect.unsafe.implicits._
import java.net.URL
import cats.data.EitherT
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger


class PasskitSpec extends FunSuite:
  import PasskitAgent.*
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

   def log[T](value: T)(using logger: Logger[IO]): IO[Unit] =
     //println(s"Main: $value")
     logger.info(s"Main: $value") *> IO.unit

  test("create pass ") {
    val u = for {
      a <- EitherT.right(IO.delay(PasskitAgent("Ian de Beer","did:prism1234567",new URL("https://google.com"))))
      p <- a.signPass()    
      x <- EitherT.right(copy(QRCode.from("did:prism1234567").file(),new File("/Users/ian/dev/gleibnif/modules/client/src/main/resources/qr2.png")))
      y <-  EitherT.right(write(new File("/Users/ian/dev/gleibnif/modules/client/src/main/resources/pass.pkpass"),p.getBytes))
      z <-  EitherT.right(base64Encode(p.getBytes()))
    } yield (x,y,z)

    u.value.flatTap(m => IO.delay(log(s"${m.toOption.map(_.toString())} done")
    )).unsafeRunSync()



  }
