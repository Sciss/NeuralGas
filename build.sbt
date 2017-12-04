name              := "NeuralGas"
organization      := "de.sciss"
version           := "2.2.0-SNAPSHOT"
scalaVersion      := "2.12.3"
licenses          := Seq("GPL v2+" -> url("https://www.gnu.org/licenses/gpl-2.0.txt"))
crossPaths        := false
autoScalaLibrary  := false

libraryDependencies ++= Seq(
  "org.scala-lang" %  "scala-library" % scalaVersion.value % "test",
  "de.sciss"       %% "pdflitz"       % "1.2.2"            % "test"
)

homepage          := Some(url(s"https://github.com/Sciss/${name.value.toLowerCase}"))
description       := "Neural network simulator based on growing neural gas (GNG)"

lazy val commonJavaOptions = Seq("-source", "1.6")

javacOptions        := commonJavaOptions ++ Seq("-target", "1.6", "-g", "-Xlint:deprecation")
javacOptions in doc := commonJavaOptions  // cf. sbt issue #355

mainClass in (Compile, run) := Some("Main")
