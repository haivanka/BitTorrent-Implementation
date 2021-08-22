name := "BitTorrentClient"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies += "com.dampcake" % "bencode" % "1.3.1"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.2.2"
libraryDependencies += "co.fs2" %% "fs2-core" % "3.1.0"
libraryDependencies += "co.fs2" %% "fs2-io" % "3.1.0"

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")