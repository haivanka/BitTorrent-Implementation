import cats.effect.{IO, IOApp, Resource}
import com.dampcake.bencode.{Bencode, Type}

import java.io.{File, FileInputStream}
import scala.jdk.CollectionConverters._

object EntryPoint extends IOApp.Simple {
  def run: IO[Unit] = {
    val bencode = new Bencode()
    val file = new File("some-torrent.torrent")
    val inputStreamResource = createInputStream(file)

    val dictionaryResource =
      inputStreamResource.map(inputStream => {
        val bytes = inputStream.readAllBytes()
        val dictionary = bencode.decode(bytes, Type.DICTIONARY)
//        dictionary.asScala.toMap
        dictionary
      })
    dictionaryResource.use(IO.println)
  }

  def createInputStream(f: File): Resource[IO, FileInputStream] =
    Resource.fromAutoCloseable(IO(new FileInputStream(f)))
}
