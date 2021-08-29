import scodec.Codec
import scodec.codecs.{uint16, uint8}

object BinaryHelper {
  val peerAddressCodec: Codec[PeerAddress] = (uint8 :: uint8 :: uint8 :: uint8 :: uint16).as[PeerAddress]

}
