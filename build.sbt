lazy val Scala3 = "3.2.2"
lazy val Scala213 = "2.13.6"
lazy val catsVersion = "2.9.0"
lazy val ceVersion = "3.4.9"
lazy val fs2Version = "3.6.1"
lazy val circeVersion = "0.14.5"
lazy val grpcVersion = "1.54.1"
lazy val googleProtoVersion = "3.22.3"
lazy val monocleVersion = "3.1.0"
lazy val scodecVersion = "1.1.37"
lazy val junitVersion = "0.11"
lazy val refinedVersion = "0.9.27"
lazy val castanetVersion = "0.1.5"
//lazy val didCommonVersion = "1.0.0"
lazy val didCommVersion = "0.3.2"
lazy val sttpVersion = "3.8.15"
lazy val tinkVersion = "1.8.0"
lazy val redis4catsVersion = "1.4.1"
lazy val openAIVersion = "0.3.1"
lazy val bouncyCastleVersion = "1.70"
lazy val titaniumVersion = "1.3.2"
lazy val munitVersion = "1.0.0-M7"
lazy val munitCEVersion = "1.0.7"
lazy val pureconfigVersion = "0.17.3"
lazy val ipfsVersion = "1.4.4"
lazy val log4catsVersion = "2.6.0"
lazy val logbackVersion = "1.4.7"
lazy val slf4jVersion = "1.7.36"

lazy val commonSettings = Seq(
  resolvers ++= Seq(
        Resolver.mavenLocal,
        "jitpack" at "https://jitpack.io",
        "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
        "releases" at "https://oss.sonatype.org/content/repositories/releases"

      ),
  libraryDependencies ++= Seq(
   //m "org.scala-lang" %% "scala3-staging" % Scala3,
    "org.typelevel" %% "cats-core" % catsVersion,
    "co.fs2" %% "fs2-core" % fs2Version,
    "co.fs2" %% "fs2-io" % fs2Version,
    "org.typelevel" %% "cats-effect" % ceVersion,
    "org.scodec" %% "scodec-bits" % scodecVersion,
    "org.scala-lang" %% "scala3-staging" % Scala3,
    "io.circe" %% "circe-yaml" % "0.14.2",
    "dev.mn8" %% "castanet" % castanetVersion,
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-effect" % ceVersion,
    "org.bouncycastle" % "bcpkix-jdk15on" % bouncyCastleVersion,
    "org.typelevel" %% "log4cats-core" % log4catsVersion,
    "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
    "com.github.ipfs" % "java-ipfs-http-client" % ipfsVersion,
    "com.github.pureconfig" %% "pureconfig-core" % pureconfigVersion,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % pureconfigVersion,

    "ch.qos.logback" % "logback-classic" % logbackVersion,
    //"org.slf4j" % "slf4j-api" % slf4jVersion,
    // "org.slf4j" % "slf4j-nop" % slf4jVersion ,
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

lazy val grpcSettings = Seq(
  libraryDependencies ++= Seq(
    "io.grpc" % "grpc-netty-shaded",
    "io.grpc" % "grpc-core",
    "io.grpc" % "grpc-protobuf",
    "io.grpc" % "grpc-stub",
    "io.grpc" % "grpc-netty-shaded"
  ).map(_ % grpcVersion)
)

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
  .settings(commonSettings: _*)
  .settings(
    name := "gleipnifCore",
    crossPaths := false,
    autoScalaLibrary := false,
    resolvers ++= Seq(
      Resolver.mavenLocal,
      "google" at "https://maven.google.com/"
    ),
    // crossScalaVersions := List(scala3, scala212),
    libraryDependencies ++= Seq(
   //   "org.scala-lang" %% "scala3-staging" % Scala3,
      "org.didcommx" % "didcomm" % didCommVersion,
      "com.apicatalog" % "titanium-json-ld" % titaniumVersion,
      "org.glassfish" % "jakarta.json" % "2.0.1",
      "com.google.crypto.tink" % "tink" % tinkVersion,
      "com.google.crypto.tink" % "tink-awskms" % tinkVersion
    )
  )

lazy val protocol = project
  .in(file("modules/protocol"))
  .settings(
    name := "gleipnifProtocol",
    description := "Protobuf definitions",
   /*  Compile / PB.targets := Seq(
      //PB.gens.java -> (Compile / sourceManaged).value,
      scalapb.gen() -> (Compile / sourceManaged).value,
      scalapb.gen(flatPackage = true) -> (Compile / sourceManaged).value
    ), */
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java" % googleProtoVersion % "protobuf",
      
    )
  )
  .enablePlugins(Fs2Grpc)

lazy val client = project
  .in(file("modules/client"))
  .settings(
    name := "gleipnifClient",
    description := "Protobuf Client",
    Compile / mainClass := Some("dev.mn8.gleipnif.Main"),
    resolvers ++= Seq(
      Resolver.mavenLocal,
      "jitpack" at "https://jitpack.io",
      "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "releases" at "https://oss.sonatype.org/content/repositories/releases"

    ),
    libraryDependencies ++= Seq(
      /*
      <groupId>com.github.kenglxn.QRGen</groupId>
            <artifactId>javase</artifactId>
            <version>3.0.1</version>
      */
      "com.github.kenglxn.QRGen" % "javase" % "3.0.1",
    //  "org.scala-lang" %% "scala3-staging" % Scala3,
     // "decentralized-identity" % "did-common-java" % didCommonVersion,
      "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "circe" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "cats" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % sttpVersion,
      "com.apicatalog" % "titanium-json-ld" % titaniumVersion,
      "org.glassfish" % "jakarta.json" % "2.0.1",
      "org.didcommx" % "didcomm" % "0.3.2",
      "io.cequence" %% "openai-scala-client" % openAIVersion,
      "de.brendamour" % "jpasskit" % "0.3.1-SNAPSHOT",
      "com.google.crypto.tink" % "tink" % tinkVersion
    ),
    scalapbCodeGeneratorOptions += CodeGeneratorOption.FlatPackage
  )
  .settings(commonSettings: _*)
  .settings(grpcSettings: _*)
  .enablePlugins(Fs2Grpc)
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
      dockerBaseImage := "openjdk:21-jdk-slim",
      Docker / packageName := "dawnpatrol",
      Docker / version := "latest"
    )
  .dependsOn(protocol)
  .dependsOn(core)
  .dependsOn(protocol % "protobuf")

lazy val server = project
  .in(file("modules/server"))
  .settings(commonSettings: _*)
  .settings(grpcSettings: _*)
  .settings(
    scalaVersion := Scala3,
    name := "gleipnifServer",
    description := "Protobuf Server",
    // nativeImageVersion := "21.2.0",
    Compile / mainClass := Some("dev.mn8.gleipnif.Main"),
    libraryDependencies ++= List(
      "dev.mn8" %% "castanet" % castanetVersion,
      "org.typelevel" %% "cats-core" % catsVersion,
      "dev.profunktor" %% "redis4cats-effects" % redis4catsVersion,
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
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
      dockerBaseImage := "openjdk:21-jdk-slim",
      Docker / packageName := "dwn-grpc-server",
      Docker / version := "latest"
    )
  .dependsOn(protocol)
  .dependsOn(protocol % "protobuf")
