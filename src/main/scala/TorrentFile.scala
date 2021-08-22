case class TorrentFile(info: Info, announce: Announce)

case class Info(dict: Map[String, AnyRef])

case class Announce(url: String)

object TorrentFile {
  def fromDictionary(dict: Map[String, AnyRef]): TorrentFile = ???
}
