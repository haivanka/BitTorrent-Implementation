import cats.effect.IO
import fs2.io.net.Socket

object PeerCommunication {
  def apply(socket: Socket[IO]): IO[Unit] = {
    IO.println(socket)
  }
}
