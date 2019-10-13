ThisBuild / organization := "com.mchange"
ThisBuild / version      := "0.0.1"
ThisBuild / scalaVersion := "2.12.10"

lazy val root = (project in file(".")).settings (
  name                    := "eth-quip",
  ethcfgScalaStubsPackage := "com.mchange.sc.v1.ethquip.contract",
  libraryDependencies     += "org.specs2" %% "specs2-core" % "4.0.2" % "test",
  pomExtra := pomExtraForProjectName( name.value ),

  Test / parallelExecution         := false,
  Test / ethcfgAutoDeployContracts := Seq( "Quip", "ERC20Mintable" )
)

lazy val clientPlugin = (project in file("client-plugin")).dependsOn(root).settings (
  name      := "eth-quip-client-plugin",
  sbtPlugin := true,
  addSbtPlugin("com.mchange" % "sbt-ethereum" % "0.3.2"),
  pomExtra := pomExtraForProjectName( name.value )
)

// repositories stuff

val nexus = "https://oss.sonatype.org/"
val nexusSnapshots = nexus + "content/repositories/snapshots";
val nexusStaging = nexus + "service/local/staging/deploy/maven2";

// publication, pom extra stuff

def pomExtraForProjectName( projectName : String ) = {
    <url>https://github.com/swaldman/{projectName}</url>
    <licenses>
      <license>
        <name>GNU Lesser General Public License, Version 2.1</name>
        <url>http://www.gnu.org/licenses/lgpl-2.1.html</url>
        <distribution>repo</distribution>
      </license>
      <license>
        <name>Eclipse Public License, Version 1.0</name>
        <url>http://www.eclipse.org/org/documents/epl-v10.html</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:swaldman/{projectName}.git</url>
      <connection>scm:git:git@github.com:swaldman/{projectName}</connection>
    </scm>
    <developers>
      <developer>
        <id>swaldman</id>
        <name>Steve Waldman</name>
        <email>swaldman@mchange.com</email>
      </developer>
    </developers>
}

ThisBuild / resolvers += ("staging" at nexusStaging)
ThisBuild / resolvers += ("snapshots" at nexusSnapshots)
ThisBuild / publishTo := {
  if (isSnapshot.value) Some("snapshots" at nexusSnapshots ) else Some("staging"  at nexusStaging )
}

