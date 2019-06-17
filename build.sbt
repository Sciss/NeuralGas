import sbt.Keys.libraryDependencies

lazy val baseName       = "NeuralGas"
lazy val baseNameL      = baseName.toLowerCase()
lazy val baseDescr      = "Neural network simulator based on growing neural gas (GNG)"

lazy val projectVersion = "2.4.0"
lazy val mimaVersion    = "2.4.0"

lazy val commonJavaOptions = Seq("-source", "1.6")

lazy val githubUser     = "Sciss"
lazy val projectURL	= url(s"https://github.com/$githubUser/$baseName")

lazy val gpl2   = "GPL v2+"     -> url("https://www.gnu.org/licenses/gpl-2.0.txt")
lazy val lgpl2  = "LGPL v2.1+"  -> url("https://www.gnu.org/licenses/lgpl-2.1.txt")

lazy val commonSettings = Seq(
  organization        := "de.sciss",
  version             := projectVersion,
  scalaVersion        := "2.12.8",
  crossScalaVersions  := Seq("2.12.8", "2.13.0"),
  homepage            := Some(projectURL)
) ++ publishSettings

lazy val javaSettings = Seq(
  crossPaths          := false,
  autoScalaLibrary    := false,
  javacOptions        := commonJavaOptions ++ Seq("-target", "1.6", "-g", "-Xlint:deprecation"),
  javacOptions in doc := commonJavaOptions,  // cf. sbt issue #355
  publishArtifact := {
    val old = publishArtifact.value
    old && scalaVersion.value.startsWith("2.12")  // only publish once when cross-building
  }
)

lazy val deps = new {
  val test = new {
    val fileUtil  = "1.1.3"
    val jzy3d     = "1.0.2"
    val pdflitz   = "1.4.1"
    val swingPlus = "0.4.2"
  }
}

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
    )
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
  .aggregate(core, ui, sphere)
  .settings(commonSettings)
  .settings(
    name        := baseName,
    description := baseDescr,
    licenses    := Seq(lgpl2)
  )

lazy val core = project.withId(s"$baseNameL-core").in(file("core"))
  .settings(commonSettings)
  .settings(javaSettings)
  .settings(
    name        := s"$baseName-core",
    licenses    := Seq(gpl2),
    description := s"$baseDescr - algorithms",
    libraryDependencies ++= Seq(
      "org.scala-lang" %  "scala-library" % scalaVersion.value % Test
    ),
    mimaPreviousArtifacts := Set("de.sciss" % s"$baseNameL-core" % mimaVersion)
  )

lazy val ui = project.withId(s"$baseNameL-ui").in(file("ui"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(javaSettings)
  .settings(
    name        := s"$baseName-ui",
    licenses    := Seq(gpl2),
    description := s"$baseDescr - user interface",
    mainClass in (Compile, run) := Some("de.sciss.neuralgas.ui.Main"),
    libraryDependencies ++= Seq(
      "org.scala-lang" %  "scala-library" % scalaVersion.value % Test,
      "de.sciss"       %% "fileutil"      % deps.test.fileUtil % Test,
      "de.sciss"       %% "pdflitz"       % deps.test.pdflitz  % Test
    ),
    mimaPreviousArtifacts := Set("de.sciss" % s"$baseNameL-ui" % mimaVersion)
  )

lazy val sphere = project.withId(s"$baseNameL-sphere").in(file("sphere"))
  .settings(commonSettings)
  .settings(
    name        := s"$baseName-sphere",
    licenses    := Seq(lgpl2),
    description := "GNG-U implementation in Scala for spherical coordinates",
    resolvers   += "jzv3d releases" at "http://maven.jzy3d.org/releases",
    libraryDependencies ++= Seq(
      "org.jzy3d" %   "jzy3d-api" % deps.test.jzy3d     % Test,
      "de.sciss"  %%  "swingplus" % deps.test.swingPlus % Test
    )
    // mimaPreviousArtifacts := Set("de.sciss" %% s"$baseNameL-sphere" % mimaVersion)
  )
