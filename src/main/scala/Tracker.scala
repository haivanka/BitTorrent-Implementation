import benc.BDecoder.instance
import benc.{BDecoder, Benc}
import cats.effect.IO
import scodec.bits.{BitVector, ByteVector}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.{UriContext, asByteArrayAlways, basicRequest}

import scala.concurrent.duration.{Duration, DurationInt}

case class AnnounceResponse(interval: Duration, peerAddresses: List[PeerAddress])

object AnnounceResponse {
  implicit val announceResponseDecoder: BDecoder[AnnounceResponse] = instance { bt =>
    Right(AnnounceResponse(1.seconds, List.empty))
  }
}

object Tracker {
  val defaultPeerId: ByteVector = ByteVector.view("-TRTS01-".getBytes).padRight(20)

  def sendFirstAnnounce(torrent: Torrent): IO[AnnounceResponse] = {
    for {
      backend <- AsyncHttpClientCatsBackend[IO]()
      request = createRequestWithParams(torrent.announce.url, createRequestParams(torrent))
      response <- backend.send(request)
      announceResponse <- IO.fromEither(response.body)
    } yield announceResponse
  }

  private def createRequestWithParams(announceUrl: String, params: Map[String, String]) =
    basicRequest
      .get(uri"$announceUrl".addParams(params))
      .response(asByteArrayAlways)
      .mapResponse(BitVector(_))
      .mapResponse(Benc.fromBenc[AnnounceResponse])

  private def createRequestParams(torrent: Torrent): Map[String, String] = {
    Map(
      "info_hash" -> encode(torrent.info.infoHash),
      "peer_id" -> encode(defaultPeerId),
      "port" -> "6881",
      "uploaded" -> "0",
      "downloaded" -> "0",
      "corrupt" -> "0",
      "compact" -> "1",
      "event" -> "started",
      "left" -> s"${torrent.info.length}"
    )
  }

  private def encode(byteVector: ByteVector): String = ???
}
