import NetworkUtil.Handshake
import scodec.Codec
import scodec.codecs.{bytes, fixedSizeBytes, string, uint16, uint8}

import java.nio.charset.StandardCharsets.UTF_8

object Codecs {
  val peerAddressCodec: Codec[PeerAddress] = (uint8 :: uint8 :: uint8 :: uint8 :: uint16).as[PeerAddress]

  val handshakeCodec: Codec[Handshake] = (
      uint8 ::
      fixedSizeBytes(19, string(UTF_8)) ::
      bytes(8) ::
      bytes(20) ::
      bytes(20)
    ).as[Handshake]
}
