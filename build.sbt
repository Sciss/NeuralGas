import sbt.Keys.libraryDependencies

lazy val baseName       = "NeuralGas"
lazy val baseNameL      = baseName.toLowerCase()
lazy val baseDescr      = "Neural network simulator based on growing neural gas (GNG)"

lazy val projectVersion = "2.2.0-SNAPSHOT"
lazy val mimaVersion    = "2.2.0"

lazy val commonJavaOptions = Seq("-source", "1.6")

lazy val commonSettings = Seq(
  organization        := "de.sciss",
  version             := "2.2.0-SNAPSHOT",
  scalaVersion        := "2.12.3",
  licenses            := Seq("GPL v2+" -> url("https://www.gnu.org/licenses/gpl-2.0.txt")),
  crossPaths          := false,
  autoScalaLibrary    := false,
  homepage            := Some(url(s"https://github.com/Sciss/${name.value.toLowerCase}")),
  javacOptions        := commonJavaOptions ++ Seq("-target", "1.6", "-g", "-Xlint:deprecation"),
  javacOptions in doc := commonJavaOptions  // cf. sbt issue #355
)

lazy val root = project.in(file("."))
  .aggregate(core, ui)
  .settings(commonSettings)
  .settings(
    name        := baseName,
    description := baseDescr
  )

lazy val core = project.in(file("core"))
  .settings(commonSettings)
  .settings(
    name        := s"$baseName-core",
    description := s"$baseDescr - algorithms",
    libraryDependencies ++= Seq(
      "org.scala-lang" %  "scala-library" % scalaVersion.value % "test"
    )
  )

lazy val ui = project.in(file("ui"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name        := s"$baseName-ui",
    description := s"$baseDescr - user interface",
    mainClass in (Compile, run) := Some("de.sciss.neuralgas.ui.Main"),
    libraryDependencies ++= Seq(
      "org.scala-lang" %  "scala-library" % scalaVersion.value % "test",
      "de.sciss"       %% "fileutil"      % "1.1.3"            % "test",
      "de.sciss"       %% "pdflitz"       % "1.2.2"            % "test"
    )
  )

