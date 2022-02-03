ThisBuild / organization := "io.mth"
ThisBuild / version      := "1.0.0"
ThisBuild / scalaVersion := props.ProjectScalaVersion

lazy val pirate =
  (project in file("."))
    .settings(name := props.RepoName)
    .aggregate(pirateScalaz)

lazy val pirateScalaz = projectCommonSettings("scalaz", "scalaz", file("modules/scalaz"))

lazy val props = new {

  val RepoName = "pirate"

  val Scala3Versions      = List("3.0.2")
  val ProjectScalaVersion = "2.13.5"

  val HedgehogVersion = "0.8.0"
}

lazy val libs = new {

  lazy val hedgehog = Seq(
    "qa.hedgehog" %% "hedgehog-core"   % props.HedgehogVersion,
    "qa.hedgehog" %% "hedgehog-runner" % props.HedgehogVersion,
    "qa.hedgehog" %% "hedgehog-sbt"    % props.HedgehogVersion
  ).map(_ % Test)

  lazy val scalaz: Seq[ModuleID] = Seq(
    "org.scalaz" %% "scalaz-core"   % "7.2.31",
    "org.scalaz" %% "scalaz-effect" % "7.2.31"
  )

}

def isScala3(scalaVersion: String): Boolean = scalaVersion.startsWith("3")

def prefixedProjectName(name: String) = s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"

def projectCommonSettings(id: String, projectName: String, file: File): Project =
  Project(id, file)
    .settings(
      name                              := prefixedProjectName(projectName),
      crossScalaVersions                := List("2.12.13", "2.13.5") ++ props.Scala3Versions,
      Compile / console / scalacOptions := Seq("-language:_", "-feature"),
      Test / console / scalacOptions    := Seq("-language:_", "-feature"),
      Compile / unmanagedSourceDirectories ++= {
        val sharedSourceDir = baseDirectory.value / "src/main"
        if (isScala3(scalaVersion.value)) {
          Seq(sharedSourceDir / "scala-3")
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
        if (isScala3(scalaVersion.value))
          Seq(sharedSourceDir / "scala-2.13", sharedSourceDir / "scala-3")
        else if (scalaVersion.value.startsWith("2.13"))
          Seq(sharedSourceDir / "scala-2.11_2.13", sharedSourceDir / "scala-2.13")
        else
          Seq(sharedSourceDir / "scala-2.11_2.13", sharedSourceDir / "scala-2.13-")
      },
      testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework")),
      libraryDependencies ++= libs.scalaz.map(_.cross(CrossVersion.for3Use2_13)) ++
        libs.hedgehog,
      libraryDependencies               :=
        (libraryDependencies.value ++ (
          if (isScala3(scalaVersion.value))
            Seq.empty[ModuleID]
          else
            Seq("com.chuusai" %% "shapeless" % "2.3.3")
        )).distinct,
    )
