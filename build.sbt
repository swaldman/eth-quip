ThisBuild / organization := "com.mchange"
ThisBuild / version := "0.0.1-SNAPSHOT"

lazy val root = (project in file(".")).settings (
  name                    := "eth-quip",
  scalaVersion            := "2.12.10",
  ethcfgScalaStubsPackage := "com.mchange.sc.v1.ethquip.contract",
  libraryDependencies     += "org.specs2" %% "specs2-core" % "4.0.2" % "test",

  Test / parallelExecution         := false,
  Test / ethcfgAutoDeployContracts := Seq( "Quip", "ERC20Mintable" )
)



