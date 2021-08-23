import benc._
import cats.effect.{IO, IOApp, Resource}
import scodec.bits.BitVector

import java.io.{File, FileInputStream}
import scala.jdk.CollectionConverters._

import Torrent.torrentDecoder

object EntryPoint extends IOApp.Simple {
//  def run: IO[Unit] = {
//    val bencode = new Bencode(true)
//    val file = new File("ubuntu.torrent")
//    val inputStreamResource = createInputStream(file)
//
//    val dictionaryResource =
//      inputStreamResource.map(inputStream => {
//        val bytes = inputStream.readAllBytes()
//        val dictionary = bencode.decode(bytes, Type.DICTIONARY)
//        dictionary.asScala.toMap
//      })
//    //dictionaryResource.map(dictionary => dictionary.get("info")).use(IO.println)
//    dictionaryResource
//      .map(dictionary => dictionary.get("info") match {
//        case Some(x@java.util.LinkedHashMap) => x.asScala.toMap
//        case None => Map[String, AnyRef]()
//      })
//      .map(infoDictionary => infoDictionary.get("length"))
//      .use(IO.println)
//  }

  def run: IO[Unit] = {
    val file = new File("ubuntu.torrent")
//    val inputStream = new FileInputStream(file)
//    val bytes = inputStream.readAllBytes()
//    val bitVector = BitVector(bytes)
//
//    val x = Benc.fromBenc[Torrent](bitVector)
//
//    println(x)
//
//    IO ()

    for {
      inputStream <- createInputStream(file)
      torrent <- {
        val bytes = inputStream.readAllBytes()
        val bitVector = BitVector(bytes)
        Benc.fromBenc[Torrent](bitVector).toOption
      }
    } yield torrent
//
//    val dictionaryResource =
//      inputStreamResource.(inputStream => {
//        val bytes = inputStream.readAllBytes()
//        val bitVector = BitVector(bytes)
//        val dictionary = BDecoder[Torrent].decode(bitVector)
//        dictionary
//      })
//    //dictionaryResource.map(dictionary => dictionary.get("info")).use(IO.println)
//    dictionaryResource
//      .map(dictionary => dictionary.get("info") match {
//        case Some(x@java.util.LinkedHashMap) => x.asScala.toMap
//        case None => Map[String, AnyRef]()
//      })
//      .map(infoDictionary => infoDictionary.get("length"))
//      .use(IO.println)
  }

  def createInputStream(f: File): Resource[IO, FileInputStream] =
    Resource.fromAutoCloseable(IO(new FileInputStream(f)))
}
