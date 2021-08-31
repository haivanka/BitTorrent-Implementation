import Codecs.handshakeCodec
import NetworkUtil.Handshake
import PeerCommunication.{convertAttemptToIO, convertHandshakeToChunk}
import cats.effect.{IO, Ref, Resource}
import com.comcast.ip4s.SocketAddress
import fs2.Chunk
import fs2.io.net.{Network, Socket}
import scodec.Attempt

import scala.concurrent.duration.DurationInt

case class PeerCommunication(socket: Socket[IO]) {
  def start(handshake: Handshake): IO[Unit] = {
    for {
      chunk <- convertHandshakeToChunk(handshake)
      _ <- socket.write(chunk)
      _ <- log("Sent handshake!")
      response <- socket.readN(68).timeout(200.millis)
      _ <- log(s"Read ${response.size} bytes!")
      responseHandshake <- convertAttemptToIO(
        handshakeCodec.decode(response.toBitVector))
      _ <- IO.sleep(5.seconds)
      address <- socket.remoteAddress
      _ <- log(s"Finished!")
    } yield ()
  }.handleError { f =>
    println(s"Failed due to: ${f.getMessage}")
    IO.unit
  }

  def log(msg: String): IO[Unit] = socket.remoteAddress.map(address => println(s"[${address.host}]: $msg"))
}

object PeerCommunication {
  def apply(peerAddress: PeerAddress, activePeersRef: Ref[IO, Set[PeerAddress]])
    : Resource[IO, PeerCommunication] = {
    Network[IO]
      .client(SocketAddress.fromString(peerAddress.ip).get)
      .flatMap { socket =>
        val onClose = activePeersRef.update(currentPeerAddresses =>
          currentPeerAddresses - peerAddress)
        Resource.make(IO(PeerCommunication(socket)))(_ => onClose)
      }
  }

  def convertHandshakeToChunk(handshake: Handshake): IO[Chunk[Byte]] = {
    convertAttemptToIO(handshakeCodec.encode(handshake))
      .map(bitVector => bitVector.toByteVector)
      .map(byteVector => Chunk.byteVector(byteVector))
  }

  def convertAttemptToIO[A](attempt: Attempt[A]): IO[A] =
    IO.fromTry(attempt.toTry)
}
