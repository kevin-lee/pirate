ThisBuild / organization := "io.mth"
ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := props.ProjectScalaVersion

lazy val pirate =
  (project in file("."))
    .settings(name := "pirate")
    .settings(
      crossScalaVersions := List("2.12.13", "2.13.5") ++ props.Scala3Versions,
      scalacOptions := {
          if (isScala3_0(scalaVersion.value))
            Seq.empty
          else
            Nil
        } ++ Seq(
          "-deprecation"
        , "-unchecked"
        , "-feature"
        , "-Xfatal-warnings"
        ) ++ (
        if (isScala3_0(scalaVersion.value))
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
      ),
      Compile / console / scalacOptions := Seq("-language:_", "-feature"),
      Test / console / scalacOptions := Seq("-language:_", "-feature"),
      Test / scalacOptions := (
        if (isScala3_0(scalaVersion.value))
          Seq(props.scala3cLanguageOptions)
        else
          Seq("-Yrangepos", "-language:_")
      ),
      Compile / doc / scalacOptions := ((Compile / doc / scalacOptions).value.filterNot(
        if (isScala3_0(scalaVersion.value)) {
          Set(
            "-source:3.0-migration",
            "-scalajs",
            "-deprecation",
            "-explain-types",
            "-explain",
            "-feature",
            props.scala3cLanguageOptions,
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
      )),
      Compile / unmanagedSourceDirectories ++= {
        val sharedSourceDir = baseDirectory.value / "src/main"
        if (isScala3_0(scalaVersion.value)) {
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
      },
      Test / unmanagedSourceDirectories ++= {
        val sharedSourceDir = baseDirectory.value / "src/test"
        if (isScala3_0(scalaVersion.value))
          Seq(sharedSourceDir / "scala-2.13", sharedSourceDir / "scala-3.0")
        else if (scalaVersion.value.startsWith("2.13"))
          Seq(sharedSourceDir / "scala-2.11_2.13", sharedSourceDir / "scala-2.13")
        else
          Seq(sharedSourceDir / "scala-2.11_2.13", sharedSourceDir / "scala-2.13-")
      },
      testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework")),
//    , resolvers += "bintray-scala-hedgehog" at "https://dl.bintray.com/hedgehogqa/scala-hedgehog-maven"
      libraryDependencies ++= libs.scalaz.map(_.cross(CrossVersion.for3Use2_13)) ++
        libs.hedgehog(scalaVersion.value).map(_.cross(CrossVersion.for3Use2_13)),
      libraryDependencies :=
        (libraryDependencies.value ++ (
          if (isScala3_0(scalaVersion.value))
            Seq.empty[ModuleID]
          else
            Seq("com.chuusai" %% "shapeless" % "2.3.3")
        )).distinct,
      reporterConfig ~= (
          _.withColumnNumbers(true)
           .withSourcePathColor(scala.Console.MAGENTA + scala.Console.UNDERLINED)
        ),
    )

lazy val props = new {

  val Scala3Versions = List("3.0.0")
  //val ProjectScalaVersion = DottyVersion
  val ProjectScalaVersion = "2.13.5"

  lazy val scala3cLanguageOptions = "-language:" + List(
    "dynamics",
    "existentials",
    "higherKinds",
    "reflectiveCalls",
    "experimental.macros",
    "implicitConversions"
  ).mkString(",")
}

lazy val libs = new {

  def hedgehog(scalaVersion: String): Seq[ModuleID] = {
    val hedgehogVersion = "0.7.0"
    Seq(
      "qa.hedgehog" %% "hedgehog-core" % hedgehogVersion,
      "qa.hedgehog" %% "hedgehog-runner" % hedgehogVersion,
      "qa.hedgehog" %% "hedgehog-sbt" % hedgehogVersion
    ).map(_ % Test)
  }

  lazy val scalaz: Seq[ModuleID] = Seq(
    "org.scalaz" %% "scalaz-core" % "7.2.31",
    "org.scalaz" %% "scalaz-effect" % "7.2.31"
  )

}

def isScala3_0(scalaVersion: String): Boolean = scalaVersion.startsWith("3.0")