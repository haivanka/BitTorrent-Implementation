import benc.BDecoder.{instance, utf8StringBDecoder}
import benc.{BDecoder, BencError}

final case class Info(pieceLength: Int)

final case class Announce(url: String)

final case class Torrent(info: Info, announce: Announce)

object Torrent {
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
      pieceLength <- BDecoder[Int].decode(pieceLengthBencoded).toOption
    } yield Info(pieceLength)
    optionInfo.toRight(BencError.CodecError("Empty"))
  }

  implicit val announceDecoder: BDecoder[Announce] = utf8StringBDecoder.map(Announce)
}
