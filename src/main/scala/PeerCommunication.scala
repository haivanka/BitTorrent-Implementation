import Codecs.handshakeCodec
import Network.Handshake
import cats.effect.IO
import fs2.io.net.Socket
import scodec.bits.ByteVector

object PeerCommunication {
  def apply(socket: Socket[IO]): IO[Unit] = {
    IO.println(socket)
  }

  def apply(socket: Socket[IO], infoHash: ByteVector, peerId: ByteVector): IO[Unit] = {
    val handshake = Handshake(infoHash, peerId)
    val encodedHandshake = handshakeCodec.encode(handshake)
    IO.println(encodedHandshake)
//  }
}
