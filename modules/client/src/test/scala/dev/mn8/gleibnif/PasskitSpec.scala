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


class PasskitSpec extends FunSuite:
  import PasskitAgent.*

  test("create pass ") {
    val u = for {
      a <- IO.delay(PasskitAgent("Ian de Beer","did:prism1234567","https://google.com"))
      p <- a.signPass()    
      x <- copy(QRCode.from("did:prism1234567").file(),new File("/Users/ian/dev/gleibnif/modules/client/src/main/resources/qr2.png"))
      y <- write(new File("/Users/ian/dev/gleibnif/modules/client/src/main/resources/pass.pkpass"),p)
      z <- base64Encode(p)
    } yield ((x,y,z))
    u.flatTap(m => IO(println(s"Wrote files: ${m._1}, ${m._2}\n${{m._3}}"))).unsafeRunSync()



  }
