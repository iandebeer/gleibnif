lazy val Scala3 = "3.2.1"
lazy val Scala213 = "2.13.6"
lazy val catsVersion = "2.9.0"
lazy val ceVersion = "3.4.4"
lazy val fs2Version = "3.4.0"
lazy val circeVersion = "0.14.3"
lazy val grpcVersion = "1.51.1"
lazy val googleProtoVersion = "3.21.12"
lazy val monocleVersion = "3.1.0"
lazy val scodecVersion = "1.1.34"
lazy val junitVersion = "0.11"
lazy val refinedVersion = "0.9.27"
lazy val castanetVersion = "0.1.5"
lazy val didCommonVersion = "1.1.0"
lazy val didCommVersion = "0.3.2"
lazy val sttpVersion = "3.8.8"
lazy val tinkVersion = "1.7.0"


lazy val munitVersion = "0.7.29"
lazy val munitCEVersion = "1.0.7"

ThisBuild / version := "0.0.1"
Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / organization := "dev.mn8"
ThisBuild / organizationName := "MN8 Technology ÖU"
ThisBuild / organizationHomepage := Some(url("https://mn8.dev"))
Global / scalaVersion := Scala3

lazy val root = project
  .in(file("."))
  .aggregate(core, protocol, client, server)
  .settings(
    publish / skip := true,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value
      .withOverwrite(true)
  )

lazy val core = project
  .in(file("modules/core"))
  .settings(
    name := "gleipnifCore",
    crossPaths := false,
    autoScalaLibrary := false,
    // crossScalaVersions := List(scala3, scala212),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % catsVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "org.typelevel" %% "cats-effect" % ceVersion,
      "org.scodec" %% "scodec-bits" % scodecVersion,
      "org.scala-lang" %% "scala3-staging" % Scala3,
      "io.circe" %% "circe-yaml" % "0.14.2",
      "org.scalameta" %% "munit" % munitVersion % Test,
      "org.scalameta" %% "munit-scalacheck" % munitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % munitCEVersion % Test
    ),
    libraryDependencies ++= Seq(
      // "io.circe" %% "circe-yaml",
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )

lazy val protocol = project
  .in(file("modules/protocol"))
  .settings(
    name := "gleipnifProtocol",
    description := "Protobuf definitions",
    resolvers ++= Seq(
      Resolver.mavenLocal,
      "danubetech-maven-public" at "https://repo.danubetech.com/repository/maven-public/"
    ),
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java" % googleProtoVersion % "protobuf"
    )
  )
  .enablePlugins(Fs2Grpc)

lazy val client = project
  .in(file("modules/client"))
  .settings(
    name := "gleipnifClient",
    description := "Protobuf Client",
    libraryDependencies ++= Seq(
      "org.scala-lang" %% "scala3-staging" % Scala3,
      "dev.mn8" %% "castanet" % castanetVersion,
      "org.scalameta" %% "munit" % munitVersion % Test,
      "org.scalameta" %% "munit-scalacheck" % munitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % munitCEVersion % Test,
      "org.typelevel" %% "cats-core" % catsVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "org.typelevel" %% "cats-effect" % ceVersion,
      "io.grpc" % "grpc-netty-shaded" % grpcVersion,
      "io.grpc" % "grpc-core" % grpcVersion,
      "io.grpc" % "grpc-protobuf" % grpcVersion,
      "io.grpc" % "grpc-stub" % grpcVersion,
      "decentralized-identity" % "did-common-java" % didCommonVersion,
      "io.grpc" % "grpc-netty-shaded" % grpcVersion,
      "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "circe" % sttpVersion,
      "org.didcommx" % "didcomm" % didCommVersion,
      "com.google.crypto.tink" % "tink" % tinkVersion
    ),
    scalapbCodeGeneratorOptions += CodeGeneratorOption.FlatPackage
  )
  .enablePlugins(Fs2Grpc)
  .dependsOn(protocol)
  .dependsOn(protocol % "protobuf")

lazy val server = project
  .in(file("modules/server"))
  .settings(
    scalaVersion := Scala3,
    name := "gleipnifServer",
    description := "Protobuf Server",
    // nativeImageVersion := "21.2.0",
    Compile / mainClass := Some("dev.mn8.gleipnif.Main"),
    libraryDependencies ++= List(
      "dev.mn8" %% "castanet" % castanetVersion,
      "org.typelevel" %% "cats-core" % catsVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "org.typelevel" %% "cats-effect" % ceVersion,
      "io.grpc" % "grpc-netty-shaded" % grpcVersion,
      "io.grpc" % "grpc-core" % grpcVersion,
      "io.grpc" % "grpc-services" % grpcVersion
    ),
    scalapbCodeGeneratorOptions += CodeGeneratorOption.FlatPackage
  )
  .enablePlugins(Fs2Grpc)
  .enablePlugins(NativeImagePlugin)
  .dependsOn(protocol)
  .dependsOn(protocol % "protobuf")
