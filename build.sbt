ThisBuild / organization := "io.mth"
ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := "2.11.12"

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
    , scalacOptions in (Compile, console) := Seq("-language:_", "-feature")
    , scalacOptions in (Test, console) := Seq("-language:_", "-feature")
    , scalacOptions in Test := Seq("-Yrangepos")
    , libraryDependencies ++= Seq(
          "org.scalaz" %% "scalaz-core" % "7.2.23"
        , "org.scalaz" %% "scalaz-effect" % "7.2.23"
        , "org.scalaz" %% "scalaz-scalacheck-binding" % "7.2.23-scalacheck-1.13" % Test
        , "org.specs2" %% "specs2-core" % "3.9.4" % Test
        , "org.specs2" %% "specs2-scalacheck" % "3.9.4" % Test
      ) ++ (
        if (scalaVersion.value.contains("2.10")) Seq("com.chuusai"  % s"shapeless_${scalaVersion.value}" % "2.0.0")
        else                                     Seq("com.chuusai" %% s"shapeless"                       % "2.0.0")
      )
    , dependencyOverrides ++= Seq(
        "org.scalacheck" %% "scalacheck" % "1.13.5"
      )
    )
