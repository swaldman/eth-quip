ThisBuild / organization := "com.mchange"
ThisBuild / version      := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.10"

lazy val root = (project in file(".")).settings (
  name                    := "eth-quip",
  ethcfgScalaStubsPackage := "com.mchange.sc.v1.ethquip.contract",
  libraryDependencies     += "org.specs2" %% "specs2-core" % "4.0.2" % "test",

  Test / parallelExecution         := false,
  Test / ethcfgAutoDeployContracts := Seq( "Quip", "ERC20Mintable" )
)

lazy val clientPlugin = (project in file("client-plugin")).dependsOn(root).settings (
  name      := "eth-quip-client-plugin",
  sbtPlugin := true,
  addSbtPlugin("com.mchange" % "sbt-ethereum" % "0.3.1-SNAPSHOT")
)



