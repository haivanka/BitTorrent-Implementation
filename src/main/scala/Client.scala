import NetworkUtil.Handshake
import cats.effect.{IO, Ref}
import cats.instances.seq._
import cats.syntax.parallel._

object Client {
  def start(torrent: Torrent): IO[Unit] = {
    for {
      announceResponse <- Tracker.sendFirstAnnounce(torrent)
      activePeersRef <- Ref.of[IO, Set[PeerAddress]](announceResponse.peerAddresses)
      peerAddresses <- activePeersRef.get.map(_.toSeq.take(5))
      _ <- IO.println(peerAddresses)
      peerCommunicationResources = peerAddresses.map(pa => PeerCommunication.apply(pa, activePeersRef))
      handshake = Handshake(torrent.info.infoHash, Tracker.defaultPeerId)
      workers = peerCommunicationResources.map(r => r.use(pc => pc.start(handshake)))
      _ <- workers.parSequence
    } yield ()
  }
}
