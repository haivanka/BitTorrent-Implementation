import scodec.bits.ByteVector

final case class Info(pieceLength: Long, pieces: ByteVector, name: String, length: Long, bencodedInfo: ByteVector) {
  val infoHash = {
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val infoHashBytes = md.digest(bencodedInfo.toArray)
    ByteVector(infoHashBytes)
  }

  val pieceCount = pieces.length / 20
}

final case class Announce(url: String)

final case class Torrent(info: Info, announce: Announce)

