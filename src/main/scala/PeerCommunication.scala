import Codecs.handshakeCodec
import NetworkUtil.Handshake
import cats.effect.{IO, Ref, Resource}
import com.comcast.ip4s.SocketAddress
import fs2.Chunk
import fs2.io.net.{Network, Socket}

case class PeerCommunication(socket: Socket[IO]) {
  def start(handshake: Handshake): IO[Unit] = ???
}

object PeerCommunication {
  def apply(peerAddress: PeerAddress, activePeersRef: Ref[IO, Set[PeerAddress]]): Resource[IO, PeerCommunication] = {
    Network[IO].client(SocketAddress.fromString(peerAddress.ip).get)
      .flatMap { socket =>
        val onClose = activePeersRef.update(currentPeerAddresses => currentPeerAddresses - peerAddress)
        Resource.make(IO(PeerCommunication(socket)))(_ => onClose)
      }
  }
}
