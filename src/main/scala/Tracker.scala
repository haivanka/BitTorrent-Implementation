object Tracker {
  def sendFirstAnnounce(torrent: Torrent) = ???

  def createRequest(announceUrl: String, params: Map[String, String]) = ???

  def createRequestParams(torrent: Torrent): Map[String, String] = ???
}
