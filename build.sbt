ThisBuild / organization := "io.mth"
ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := "2.13.0"

lazy val hedgehogVersion = "64eccc9ca7dbe7a369208a14a97a25d7ccbbda67"
lazy val hedgehog = Seq(
    "qa.hedgehog" %% "hedgehog-core" % hedgehogVersion
  , "qa.hedgehog" %% "hedgehog-runner" % hedgehogVersion
  , "qa.hedgehog" %% "hedgehog-sbt" % hedgehogVersion
  ).map(_ % Test)

lazy val pirate =
  (project in file("."))
    .settings(name := "pirate")
    .settings(
      crossScalaVersions := Seq("2.11.12", "2.12.10", scalaVersion.value)
    , scalacOptions := Seq(
          "-deprecation"
        , "-unchecked"
        , "-feature"
        , "-language:_"
        , "-Ywarn-value-discard"
        , "-Xlint"
        , "-Xfatal-warnings"
      ) ++ (if (scalaBinaryVersion.value == "2.13") Seq("-Wunused:imports") else Seq("-Ywarn-unused-import"))
    , scalacOptions in (Compile, console) := Seq("-language:_", "-feature")
    , scalacOptions in (Test, console) := Seq("-language:_", "-feature")
    , scalacOptions in Test := Seq("-Yrangepos")
    , testFrameworks := Seq(TestFramework("hedgehog.sbt.Framework"))
    , resolvers += "bintray-scala-hedgehog" at "https://dl.bintray.com/hedgehogqa/scala-hedgehog"
    , libraryDependencies ++= Seq(
          "org.scalaz" %% "scalaz-core" % "7.2.28"
        , "org.scalaz" %% "scalaz-effect" % "7.2.28"
        , "com.chuusai" %% "shapeless" % "2.3.3"
      ) ++ hedgehog
    )
