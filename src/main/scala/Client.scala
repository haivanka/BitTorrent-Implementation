import cats.effect.{IO, Ref}
import cats.instances.seq._
import cats.syntax.parallel._

import scala.concurrent.duration.DurationInt

object Client {
  def start(torrent: Torrent): IO[Unit] = {
    for {
      peersStateRef <- Ref.of[IO, PeersState](PeersState(Map.empty))
      announceResponse <- Tracker.sendFirstAnnounce(torrent)
      peers = extractPeers(announceResponse, torrent.info.pieceCount)
      _ <- peersStateRef.update(currentPeersState => currentPeersState.addNewPeers(peers))
      state <- peersStateRef.get
      _ <- IO.println(state)
      _ <- peers.keySet.toSeq.take(5).flatMap(MessageSocket.apply).parSequence
      _ <- IO.sleep(30.seconds)
    } yield ()
  }

  private def extractPeers(announceResponse: AnnounceResponse, pieceCount: Long) =
    announceResponse.peerAddresses.map(_ -> PeerState.startingState(pieceCount)).toMap
}
