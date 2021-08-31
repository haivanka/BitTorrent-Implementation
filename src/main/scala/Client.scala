import NetworkUtil.Handshake
import cats.effect.{IO, Ref}
import cats.instances.seq._
import cats.syntax.parallel._

import scala.concurrent.duration.{DurationInt}

object Client {
  def start(torrent: Torrent): IO[Unit] = {
    for {
      announceResponse <- Tracker.sendFirstAnnounce(torrent)
      _ <- IO.println(announceResponse.peerAddresses)
      activePeersRef <- Ref.of[IO, Set[PeerAddress]](announceResponse.peerAddresses.take(10))
      peerAddresses <- activePeersRef.get.map(_.toSeq)
      _ <- IO.println(peerAddresses)
      peerCommunicationResources = peerAddresses.map(pa => PeerCommunication.apply(pa, activePeersRef))
      handshake = Handshake(torrent.info.infoHash, Tracker.defaultPeerId)
      workers = peerCommunicationResources.map(r => r.use(pc => pc.start(handshake)))
      _ <- IO.monotonic.map(println)
      _ <- workers.map(work => work.start).parSequence
      _ <- IO.monotonic.map(println)
      _ <- activePeersRef.get.map(x => println(x.size))
      _ <- IO.sleep(10.seconds)
      _ <- activePeersRef.get.map(x => println(x.size))
      _ <- IO.sleep(10.seconds)
      _ <- activePeersRef.get.map(x => println(x.size))
    } yield ()
  }
}
