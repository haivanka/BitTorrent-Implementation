import PeerAddress.parsePeerAddressesFromBits
import benc.BDecoder.{bitVectorBDecoder, instance, longBDecoder, utf8StringBDecoder}
import benc.{BDecoder, BencError}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

object Decoders {
  implicit val torrentDecoder: BDecoder[Torrent] = instance { bt =>
    val optionTorrent = for {
      m <- bt.bmap
      announceBencoded <- m.get("announce")
      announce <- BDecoder[Announce].decode(announceBencoded).toOption
      infoBencoded <- m.get("info")
      info <- BDecoder[Info].decode(infoBencoded).toOption
    } yield Torrent(info, announce)
    optionTorrent.toRight(BencError.CodecError("Empty"))
  }

  implicit val infoDecoder: BDecoder[Info] = instance { bt =>
    val optionInfo = for {
      m <- bt.bmap
      pieceLengthBencoded <- m.get("piece length")
      pieceLength <- BDecoder[Long].decode(pieceLengthBencoded).toOption
      piecesBencoded <- m.get("pieces")
      pieces <- piecesBencoded.bstring.map(_.toByteVector)
      nameBencoded <- m.get("name")
      name <- BDecoder[String].decode(nameBencoded).toOption
      lengthBencoded <- m.get("length")
      length <- BDecoder[Long].decode(lengthBencoded).toOption
      bencodedInfo <- bt.toBenc.map(_.toByteVector).toOption
    } yield Info(pieceLength, pieces, name, length, bencodedInfo)
    optionInfo.toRight(BencError.CodecError("Empty"))
  }

  implicit val announceDecoder: BDecoder[Announce] = utf8StringBDecoder.map(Announce)

  implicit val announceResponseDecoder: BDecoder[AnnounceResponse] = instance { bt =>
    val optionAnnounce = for {
      m <- bt.bmap
      intervalBencoded <- m.get("interval")
      interval <- BDecoder[Duration].decode(intervalBencoded).toOption
      peersBencoded <- m.get("peers")
      peers <-  BDecoder[Set[PeerAddress]].decode(peersBencoded).toOption
    } yield AnnounceResponse(interval, peers)
    optionAnnounce.toRight(BencError.CodecError("Empty"))
  }

  implicit val peerAddressesDecoder: BDecoder[Set[PeerAddress]] = bitVectorBDecoder.map(parsePeerAddressesFromBits)

  implicit val intervalDecoder: BDecoder[Duration] =
    longBDecoder.map(interval => Duration(interval, TimeUnit.SECONDS))
}

