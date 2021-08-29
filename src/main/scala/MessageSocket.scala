import cats.effect.IO
import com.comcast.ip4s._
import fs2.io.net.Network

object MessageSocket {
  def apply(peerAddress: PeerAddress) =
    Network[IO].client(SocketAddress.fromString(peerAddress.ip).get)

}
