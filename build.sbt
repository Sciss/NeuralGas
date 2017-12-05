import sbt.Keys.libraryDependencies

lazy val baseName       = "NeuralGas"
lazy val baseNameL      = baseName.toLowerCase()
lazy val baseDescr      = "Neural network simulator based on growing neural gas (GNG)"

lazy val projectVersion = "2.3.0"
lazy val mimaVersion    = "2.3.0"

lazy val commonJavaOptions = Seq("-source", "1.6")

lazy val githubUser     = "Sciss"
lazy val projectURL	= url(s"https://github.com/$githubUser/$baseName")

lazy val commonSettings = Seq(
  organization        := "de.sciss",
  version             := projectVersion,
  scalaVersion        := "2.12.4",
  licenses            := Seq("GPL v2+" -> url("https://www.gnu.org/licenses/gpl-2.0.txt")),
  crossPaths          := false,
  autoScalaLibrary    := false,
  homepage            := Some(projectURL),
  javacOptions        := commonJavaOptions ++ Seq("-target", "1.6", "-g", "-Xlint:deprecation"),
  javacOptions in doc := commonJavaOptions  // cf. sbt issue #355
) ++ publishSettings

lazy val publishSettings = Seq(
  developers := List(
//    Developer(
//      id    = "hartmus_s_loos",
//      name  = "Hartmut S. Loos",
//      email = "unknown@example.com",
//      url   = url("https://www.example.com/~unknown")
//    ),
    Developer(
      id    = "gittar",
      name  = "Bernd Fritzke",
      email = "fritzke@web.de",
      url   = url("https://github.com/gittar")
    ),
    Developer(
      id    = "sciss",
      name  = "Hanns Holger Rutz",
      email = "contact@sciss.de",
      url   = url("https://github.com/Sciss")
    ),
  ),
  scmInfo := Some(ScmInfo(
    projectURL,
    s"scm:git:https://github.com/$githubUser/$baseName.git",
    Some(s"scm:git:git@github.com:$githubUser/$baseName.git")
  )),
  publishMavenStyle := true,
  publishTo := {
    Some(if (isSnapshot.value)
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    else
      "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    )
  }
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

