import sbt.Keys.version

lazy val `akka-persistent-hello-world` = (project in file("."))
  .settings(
    name := "akka-persistent-hello-world",
    version := "1.0",
    scalaVersion := "2.12.1",
    libraryDependencies ++= dependencies,
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
    )
  )

lazy val dependencies = {

  val akkaV = "2.4.17"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV,
    "com.typesafe.akka" %% "akka-persistence" % akkaV,
    "com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaV,

    "org.iq80.leveldb"            % "leveldb"          % "0.7",
    "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",

    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.mockito" % "mockito-core" % "2.7.5" % "test",

    "com.chuusai" %% "shapeless" % "2.3.2",
    "org.typelevel" %% "cats" % "0.9.0"
  )
}
