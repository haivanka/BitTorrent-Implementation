import scodec.bits.ByteVector

object Network {
  case class Handshake(pstrlen: Int, pstr: String, reserved: ByteVector, infoHash: ByteVector, peerId: ByteVector)

  object Handshake {
    def apply(infoHash: ByteVector, peerId: ByteVector): Handshake =
      Handshake(19, "BitTorrent protocol", ByteVector.fill(8)(0), infoHash, peerId)
  }
}