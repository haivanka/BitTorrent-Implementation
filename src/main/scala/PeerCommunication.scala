import Codecs.handshakeCodec
import NetworkUtil.Handshake
import PeerCommunication.{convertChunkToHandshake, convertHandshakeToChunk, raiseErrorOnCondition}
import cats.effect.{IO, Ref, Resource}
import com.comcast.ip4s.SocketAddress
import fs2.Chunk
import fs2.io.net.{Network, Socket}
import scodec.Attempt

import scala.concurrent.duration.DurationInt

case class PeerCommunication(socket: Socket[IO]) {

  def start(handshake: Handshake): IO[Unit] = {
    exchangeHandshakes(handshake)
  }

  def exchangeHandshakes(handshake: Handshake): IO[Unit] = {
    for {
      chunk <- convertHandshakeToChunk(handshake)
      _ <- socket.write(chunk)
      _ <- log("Sent handshake!")
      response <- socket.readN(68).timeout(1000.millis)
      _ <- log(s"Read ${response.size} bytes!")
      responseHandshake <- convertChunkToHandshake(response)
      _ <- raiseErrorOnCondition(responseHandshake.infoHash != handshake.infoHash)("wrong info_hash!")
      _ <- log("Finished!")
    } yield ()
  }.handleErrorWith(f => log(f.toString))

  def log(msg: String): IO[Unit] =
    socket.remoteAddress.map(address => println(s"[${address.host}]: $msg"))
}

object PeerCommunication {
  def apply(peerAddress: PeerAddress, activePeersRef: Ref[IO, Set[PeerAddress]]): Resource[IO, PeerCommunication] =
    Network[IO]
      .client(SocketAddress.fromString(peerAddress.ip).get)
      .flatMap { socket =>
        val addPeerAddress = activePeersRef.update(currentPeerAddresses => currentPeerAddresses + peerAddress)
        val removePeerAddress = activePeersRef.update(currentPeerAddresses => currentPeerAddresses - peerAddress)
        val create = addPeerAddress.map(_ => PeerCommunication(socket))
        Resource.make(create)(_ => removePeerAddress)
      }

  def convertHandshakeToChunk(handshake: Handshake): IO[Chunk[Byte]] =
    convertAttemptToIO(handshakeCodec.encode(handshake))
      .map(bitVector => bitVector.toByteVector)
      .map(byteVector => Chunk.byteVector(byteVector))

  def convertChunkToHandshake(chunk: Chunk[Byte]): IO[Handshake] =
    convertAttemptToIO(handshakeCodec.decode(chunk.toBitVector))
      .map(result => result.value)

  def convertAttemptToIO[A](attempt: Attempt[A]): IO[A] =
    IO.fromTry(attempt.toTry)

  def raiseErrorOnCondition(condition: Boolean)(msg: String): IO[Unit] =
    if (condition) IO.raiseError(new Exception(msg)) else IO.unit
}
