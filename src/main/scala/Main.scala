import Torrent.torrentDecoder
import benc._
import cats.effect.{IO, IOApp, Resource}
import scodec.bits.BitVector

import java.io.{File, FileInputStream}

object Main extends IOApp.Simple {

  def run: IO[Unit] = {
    val file = new File("ubuntu.torrent")
    val inputStreamResource = createInputStreamResource(file)

    inputStreamResource.use { inputStream =>
      for {
        bytes <- IO.blocking(inputStream.readAllBytes())
        torrent <- parseTorrentFromBytes(bytes)
        _ <- Client.start(torrent)
      } yield ()
    }
  }

  def parseTorrentFromBytes(bytes: Array[Byte]): IO[Torrent] = {
    val bitVector = BitVector(bytes)
    val torrentEither = Benc.fromBenc[Torrent](bitVector)
    IO.fromEither(torrentEither)
  }

  def createInputStreamResource(f: File): Resource[IO, FileInputStream] =
    Resource.fromAutoCloseable(IO(new FileInputStream(f)))
}