import scodec.bits.BitVector

case class PeerAddress(peerId: String, ip: String, port: Int)

case class PeerState(choked: Boolean, interested: Boolean, bitfield: BitVector)

case class PeersState(peers: Map[PeerAddress, PeersState]) {
  def addNewPeers(newPeers: Map[PeerAddress, PeerState]): PeersState = copy(peers = peers.++(newPeers))
}

object PeerState {
  def startingState(pieceCount: Long): PeerState =
    PeerState(choked = true, interested = false, bitfield = BitVector.fill(pieceCount)(false))
}
