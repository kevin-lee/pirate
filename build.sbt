ThisBuild / organization := "io.mth"
ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := "2.11.12"

lazy val hedgehogVersion = "beaaaad0b182e5184b2985d18c79963ffad83d2b"
lazy val hedgehog = Seq(
    "hedgehog" %% "hedgehog-core" % hedgehogVersion
  , "hedgehog" %% "hedgehog-runner" % hedgehogVersion
  , "hedgehog" %% "hedgehog-sbt" % hedgehogVersion
  ).map(_ % Test)

lazy val pirate =
  (project in file("."))
    .settings(name := "pirate")
    .settings(
      crossScalaVersions := Seq("2.10.5", scalaVersion.value)
    , scalacOptions := Seq(
          "-deprecation"
        , "-unchecked"
        , "-feature"
        , "-language:_"
        , "-Ywarn-value-discard"
        , "-Xlint"
        , "-Xfatal-warnings"
      ) ++ (if (scalaBinaryVersion.value != "2.10") Seq("-Ywarn-unused-import") else Seq.empty)
    , scalacOptions := Seq("-language:_", "-feature")
    , scalacOptions in (Compile, console) := Seq("-language:_", "-feature")
    , scalacOptions in (Test, console) := Seq("-language:_", "-feature")
    , scalacOptions in Test := Seq("-Yrangepos")
    , testFrameworks := Seq(TestFramework("hedgehog.sbt.Framework"))
    , resolvers += Resolver.url("bintray-scala-hedgehog",
        url("https://dl.bintray.com/hedgehogqa/scala-hedgehog")
      )(Resolver.ivyStylePatterns)
    , libraryDependencies ++= Seq(
          "org.scalaz" %% "scalaz-core" % "7.2.23"
        , "org.scalaz" %% "scalaz-effect" % "7.2.23"
      ) ++ (
        if (scalaVersion.value.contains("2.10"))
          Seq(
            "com.chuusai" % "shapeless" % "2.1.0" cross CrossVersion.full
          , compilerPlugin("org.scalamacros" % "paradise_2.10.5" % "2.0.1")
          )
        else
          Seq("com.chuusai" %% s"shapeless" % "2.1.0")
      ) ++ hedgehog
    )
