import Decoders.torrentDecoder
import benc._
import cats.effect.{IO, IOApp, Resource}
import scodec.bits.BitVector

import java.io.FileInputStream

object Main extends IOApp.Simple {

  def run: IO[Unit] = {
    val path = "ubuntu.torrent"
    for {
      bytes <- createInputStreamResource(path).use { inputStream => IO.blocking(inputStream.readAllBytes()) }
      torrent <- parseTorrentFromBytes(bytes)
      _ <- IO.println(torrent)
      _ <- Client.start(torrent)
    } yield ()
  }

//  def run: IO[Unit] = {
//    val work = IO.sleep(1000.millis).map(_ => println("finished"))
//    val p1 = work
//    val p2 = work
//    for {
//      start <- IO.monotonic.map(_.toMillis)
//      _ <- p1.start
//      _ <- p2
//      end <- IO.monotonic.map(_.toMillis)
//      _ <- IO.println(end - start)
//    } yield ()
//  }

  def parseTorrentFromBytes(bytes: Array[Byte]): IO[Torrent] = {
    val bitVector = BitVector(bytes)
    val torrentEither = Benc.fromBenc[Torrent](bitVector)

    IO.fromEither(torrentEither)
  }

  def createInputStreamResource(path: String): Resource[IO, FileInputStream] =
    Resource.fromAutoCloseable(IO(new FileInputStream(path)))
}
