val DottyVersion = "3.0.0-RC1"
//val ProjectScalaVersion = DottyVersion
val ProjectScalaVersion = "2.13.3"

ThisBuild / organization := "io.mth"
ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := ProjectScalaVersion

lazy val scala3cLanguageOptions = "-language:" + List(
  "dynamics",
  "existentials",
  "higherKinds",
  "reflectiveCalls",
  "experimental.macros",
  "implicitConversions"
).mkString(",")

lazy val hedgehogVersion = "0.6.5"
lazy val hedgehog = Seq(
    "qa.hedgehog" %% "hedgehog-core" % hedgehogVersion
  , "qa.hedgehog" %% "hedgehog-runner" % hedgehogVersion
  , "qa.hedgehog" %% "hedgehog-sbt" % hedgehogVersion
  ).map(_ % Test)

lazy val pirate =
  (project in file("."))
    .settings(name := "pirate")
    .settings(
      crossScalaVersions := Seq("2.12.12", "2.13.3", DottyVersion)
    , scalacOptions :=
        {
          if (isDotty.value)
            Seq.empty
          else
            Nil
        } ++ Seq(
          "-deprecation"
        , "-unchecked"
        , "-feature"
        , "-Xfatal-warnings"
        ) ++ (
        if (isDotty.value)
          Seq(
            "-language:" + List(
              "dynamics",
              "existentials",
              "higherKinds",
              "reflectiveCalls",
              "experimental.macros",
              "implicitConversions"
            ).mkString(","),
          )
        else if (scalaBinaryVersion.value == "2.13")
          Seq(
              "-language:_"
            , "-Ywarn-value-discard"
            , "-Xlint"
            , "-Wunused:imports"
            , "-Wconf:cat=lint-byname-implicit:s"
          )
        else
          Seq(
              "-language:_"
            , "-Ywarn-value-discard"
            , "-Xlint"
            , "-Ywarn-unused-import"
          )
      )
    , Compile / console / scalacOptions := Seq("-language:_", "-feature")
    , Test / console / scalacOptions := Seq("-language:_", "-feature")
    , Test / scalacOptions := (
        if (isDotty.value)
          Seq(scala3cLanguageOptions)
        else
          Seq("-Yrangepos", "-language:_")
      )
    , Compile / doc / scalacOptions := ((Compile / doc / scalacOptions).value.filterNot(
        if (isDotty.value) {
          Set(
            "-source:3.0-migration",
            "-scalajs",
            "-deprecation",
            "-explain-types",
            "-explain",
            "-feature",
            scala3cLanguageOptions,
            "-unchecked",
            "-Xfatal-warnings",
            "-Ykind-projector",
            "-from-tasty",
            "-encoding",
            "utf8",
          )
        } else {
          Set.empty[String]
        }
      ))
    , Compile / unmanagedSourceDirectories ++= {
        val sharedSourceDir = baseDirectory.value / "src/main"
        if (isDotty.value) {
          Seq(sharedSourceDir / "scala-3.0")
        } else {
          CrossVersion.binaryScalaVersion(scalaVersion.value) match {
            case "2.13" =>
              Seq(sharedSourceDir / "scala-2.11_2.13", sharedSourceDir / "scala-2.13")

            case "2.11" | "2.12" =>
              Seq(sharedSourceDir / "scala-2.11_2.13", sharedSourceDir / "scala-2.13-")

            case _ =>
              Seq.empty
          }
        }
      }
    , Test / unmanagedSourceDirectories ++= {
        val sharedSourceDir = baseDirectory.value / "src/test"
        if (isDotty.value)
          Seq(sharedSourceDir / "scala-2.13", sharedSourceDir / "scala-3.0")
        else if (scalaVersion.value.startsWith("2.13"))
          Seq(sharedSourceDir / "scala-2.11_2.13", sharedSourceDir / "scala-2.13")
        else
          Seq(sharedSourceDir / "scala-2.11_2.13", sharedSourceDir / "scala-2.13-")
      }
    , testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework"))
//    , resolvers += "bintray-scala-hedgehog" at "https://dl.bintray.com/hedgehogqa/scala-hedgehog-maven"
    , libraryDependencies ++= Seq(
          "org.scalaz" %% "scalaz-core" % "7.2.30"
        , "org.scalaz" %% "scalaz-effect" % "7.2.30"
      ).map(_.withDottyCompat(scalaVersion.value)) ++ hedgehog
    , libraryDependencies :=
        (libraryDependencies.value ++ (
          if (isDotty.value)
            Seq.empty[ModuleID]
          else
            Seq("com.chuusai" %% "shapeless" % "2.3.3")
        )).distinct
    )
