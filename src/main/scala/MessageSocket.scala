import cats.effect.IO
import com.comcast.ip4s._
import fs2.Chunk
import fs2.io.net.Network

object MessageSocket {
  def apply(peerAddress: PeerAddress) = {
    SocketAddress.fromString(peerAddress.ip).map { socketAddress =>
      IO.println(socketAddress)
      val socketResource = Network[IO].client(socketAddress)
      socketResource.use { socket =>
//        socket.write(Chunk.array("Hello, world!".getBytes))
        IO.println(socket)
      }
    }
  }
}
