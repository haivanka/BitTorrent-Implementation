import BinaryHelper.peerAddressCodec
import scodec.Attempt.{Failure, Successful}
import scodec.DecodeResult
import scodec.bits.BitVector

case class PeerAddress(part1: Int, part2: Int, part3: Int, part4: Int, port: Int) {
  val ip: String = s"$part1.$part2.$part3.$part4:$port"
}

case class PeerState(choked: Boolean, interested: Boolean, bitfield: BitVector)

case class PeersState(peers: Map[PeerAddress, PeerState]) {
  def addNewPeers(newPeers: Map[PeerAddress, PeerState]): PeersState = copy(peers = peers ++ newPeers)
}

object PeerState {
  def startingState(pieceCount: Long): PeerState =
    PeerState(choked = true, interested = false, bitfield = BitVector.fill(pieceCount)(false))
}

object PeerAddress {
  def parsePeerAddressesFromBits(bitVector: BitVector): List[PeerAddress] = {
    peerAddressCodec.decode(bitVector) match {
      case Successful(DecodeResult(value, remainder)) => value :: parsePeerAddressesFromBits(remainder)
      case Failure(cause) => List.empty
    }
  }
}

