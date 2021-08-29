import Decoders.announceResponseDecoder
import benc.BDecoder.{instance, longBDecoder}
import benc.{BDecoder, Benc, BencError}
import cats.effect.IO
import scodec.bits.{BitVector, ByteVector}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.{UriContext, asByteArrayAlways, basicRequest}
import sttp.model.Uri

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{Duration, DurationInt}

case class AnnounceResponse(interval: Duration, peerAddresses: List[PeerAddress])

object Tracker {
  val defaultPeerId: ByteVector = ByteVector.view("-TRTS01-".getBytes).padRight(20)

  def sendFirstAnnounce(torrent: Torrent): IO[AnnounceResponse] = {
    for {
      backend <- AsyncHttpClientCatsBackend[IO]()
      request = createRequestWithParams(torrent, createRequestParams(torrent))
      _ <- IO.println(request)
      response <- backend.send(request)
      _ <- IO.println(response)
      announceResponse <- IO.fromEither(response.body)
      _ <- IO.println(announceResponse)
      _ <- backend.close()
    } yield announceResponse
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

  private def encode(byteVector: ByteVector): String = {
    URLEncoder.encode(
      new String(byteVector.toArray, StandardCharsets.ISO_8859_1),
      StandardCharsets.ISO_8859_1
    )
  }
}
