name := "BitTorrentClient"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies += "org.typelevel" %% "cats-effect" % "3.2.2"
libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.3.13"
libraryDependencies += "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % "3.3.13"
libraryDependencies += "co.fs2" %% "fs2-core" % "3.1.0"
libraryDependencies += "co.fs2" %% "fs2-io" % "3.1.0"
libraryDependencies += "com.minosiants" %% "benc" % "0.7.1"

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
