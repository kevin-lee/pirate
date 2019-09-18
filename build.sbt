ThisBuild / organization := "io.mth"
ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := "2.13.0"

lazy val hedgehogVersion = "06b22e95ca1a32a2569914824ffe6fc4cfd62c62"
lazy val hedgehog = Seq(
    "hedgehog" %% "hedgehog-core" % hedgehogVersion
  , "hedgehog" %% "hedgehog-runner" % hedgehogVersion
  , "hedgehog" %% "hedgehog-sbt" % hedgehogVersion
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
    , resolvers += Resolver.url("bintray-scala-hedgehog",
        url("https://dl.bintray.com/hedgehogqa/scala-hedgehog")
      )(Resolver.ivyStylePatterns)
    , libraryDependencies ++= Seq(
          "org.scalaz" %% "scalaz-core" % "7.2.28"
        , "org.scalaz" %% "scalaz-effect" % "7.2.28"
        , "com.chuusai" %% "shapeless" % "2.3.3"
      ) ++ hedgehog
    )
