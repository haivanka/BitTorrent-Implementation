import cats.effect.{IO, Ref}

object Client {
  def start(torrent: Torrent): IO[Unit] = {
    for {
      peersStateRef <- Ref.of[IO, PeersState](PeersState(Map.empty))
      announceResponse <- Tracker.sendFirstAnnounce(torrent)
      newPeers = extractPeers(announceResponse, torrent.info.pieceCount)
      _ <- peersStateRef.update(currentPeersState => currentPeersState.addNewPeers(newPeers))
      state <- peersStateRef.get
      _ <- IO.println(state)
    } yield ()
  }

  private def extractPeers(announceResponse: AnnounceResponse, pieceCount: Long) =
    announceResponse.peerAddresses.map(_ -> PeerState.startingState(pieceCount)).toMap
}
