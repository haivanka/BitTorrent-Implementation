import benc.BDecoder.{instance, utf8StringBDecoder}
import benc.{BDecoder, BencError}
import scodec.bits.ByteVector

final case class Info(pieceLength: Long, pieces: ByteVector, name: String, length: Long)

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
      pieceLength <- BDecoder[Long].decode(pieceLengthBencoded).toOption
      piecesBencoded <- m.get("pieces")
      pieces <- piecesBencoded.bstring.map(_.toByteVector)
      nameBencoded <- m.get("name")
      name <- BDecoder[String].decode(nameBencoded).toOption
      lengthBencoded <- m.get("length")
      length <- BDecoder[Long].decode(lengthBencoded).toOption
    } yield Info(pieceLength, pieces, name, length)
    optionInfo.toRight(BencError.CodecError("Empty"))
  }

  implicit val announceDecoder: BDecoder[Announce] = utf8StringBDecoder.map(Announce)
}
