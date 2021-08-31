import Decoders.announceResponseDecoder
import benc.Benc
import cats.effect.{IO, Resource}
import scodec.bits.{BitVector, ByteVector}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.{UriContext, asByteArrayAlways, basicRequest}
import sttp.model.Uri

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import scala.concurrent.duration.Duration

case class AnnounceResponse(interval: Duration, peerAddresses: Set[PeerAddress])

object Tracker {
  val defaultPeerId: ByteVector = ByteVector.view("valetilenka".getBytes).padRight(20)

  def sendFirstAnnounce(torrent: Torrent): IO[AnnounceResponse] = {
    Resource.make(AsyncHttpClientCatsBackend[IO]())(_.close()).use { backend =>
      val request =
        createRequestWithParams(torrent, createRequestParams(torrent))
      backend
        .send(request)
        .map(_.body)
        .flatMap(IO.fromEither(_))
    }
  }

  private def createRequestWithParams(torrent: Torrent, params: Map[String, String]) = {
    val announceUrl = torrent.announce.url
    val infoHash = encode(torrent.info.infoHash)
    val peerId = encode(defaultPeerId)

    basicRequest
      .get(uri"$announceUrl"
        .addQuerySegment(
          Uri.QuerySegment.KeyValue("info_hash", infoHash, valueEncoding = identity)
        )
        .addQuerySegment(
          Uri.QuerySegment.KeyValue("peer_id", peerId, valueEncoding = identity)
        )
        .addParams(params))
      .response(asByteArrayAlways)
      .mapResponse(BitVector(_))
      .mapResponse(Benc.fromBenc[AnnounceResponse])
  }

  private def encode(byteVector: ByteVector): String = {
    URLEncoder.encode(
      new String(byteVector.toArray, StandardCharsets.ISO_8859_1),
      StandardCharsets.ISO_8859_1
    )
  }

  private def createRequestParams(torrent: Torrent): Map[String, String] = {
    Map(
      "port" -> "6881",
      "uploaded" -> "0",
      "downloaded" -> "0",
      "corrupt" -> "0",
      "compact" -> "1",
      "event" -> "started",
      "left" -> s"${torrent.info.length}"
    )
  }
}
