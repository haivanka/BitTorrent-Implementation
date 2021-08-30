import Codecs.peerAddressCodec
import scodec.Attempt.{Failure, Successful}
import scodec.DecodeResult
import scodec.bits.BitVector

case class PeerAddress(part1: Int, part2: Int, part3: Int, part4: Int, port: Int) {
  val ip: String = s"$part1.$part2.$part3.$part4:$port"
}

case class PeerState(choked: Boolean, interested: Boolean, bitfield: BitVector)

object PeerAddress {
  def parsePeerAddressesFromBits(bitVector: BitVector): Set[PeerAddress] = {
    peerAddressCodec.decode(bitVector) match {
      case Successful(DecodeResult(value, remainder)) => Set(value) ++ parsePeerAddressesFromBits(remainder)
      case Failure(cause) => Set.empty
    }
  }
}

