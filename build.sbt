ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.12"
ThisBuild / organization := "org.example"

val spinalVersion = "1.10.1"
val spinalCore = "com.github.spinalhdl" %% "spinalhdl-core" % spinalVersion
val spinalLib = "com.github.spinalhdl" %% "spinalhdl-lib" % spinalVersion
val spinalIdslPlugin = compilerPlugin("com.github.spinalhdl" %% "spinalhdl-idsl-plugin" % spinalVersion)

lazy val SpinalVerficationMethodology = (project in file("."))
  .settings(
    Compile / scalaSource := baseDirectory.value / "svm" ,
    libraryDependencies ++= Seq(spinalCore, spinalLib, spinalIdslPlugin),
    Test / scalaSource := baseDirectory.value / "test"
  )

fork := true
