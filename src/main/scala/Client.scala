import cats.effect.{IO, Ref}
import cats.instances.seq._
import cats.syntax.parallel._
import com.comcast.ip4s.SocketAddress
import fs2.io.net.Network

import scala.concurrent.duration.DurationInt

object Client {
  def start(torrent: Torrent): IO[Unit] = {
    for {
      announceResponse <- Tracker.sendFirstAnnounce(torrent)
      initialPeersState = createInitialPeersState(announceResponse, torrent.info.pieceCount)
      peersStateRef <- Ref.of[IO, PeersState](initialPeersState)
      state <- peersStateRef.get
      _ <- IO.println(state)
      peerAddresses = initialPeersState.peers.keySet.toSeq
      socketResources = createSocketResources(peerAddresses)
      peersWork = socketResources.map {
        socketResource => socketResource.use(socket => PeerCommunication.apply(socket, PeerState.startingState(10)))
      }
      _ <- peersWork.parSequence
    } yield ()
  }

  private def createSocketResources(peerAddresses: Seq[PeerAddress]) = {
    peerAddresses.map { peerAddress =>
      Network[IO].client(SocketAddress.fromString(peerAddress.ip).get)
    }
  }

  private def createInitialPeersState(announceResponse: AnnounceResponse, pieceCount: Long) = {
    val peersMap =
      announceResponse.peerAddresses.map(peerAddress => peerAddress -> PeerState.startingState(pieceCount)).toMap
    PeersState(peersMap)
  }
}
