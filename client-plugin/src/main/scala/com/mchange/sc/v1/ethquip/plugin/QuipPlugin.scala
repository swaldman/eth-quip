package com.mchange.sc.v1.ethquip.plugin

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbt.Def.Initialize

import sbt.complete.DefaultParsers._

import com.mchange.sc.v1.sbtethereum.SbtEthereumPlugin
import com.mchange.sc.v1.sbtethereum.SbtEthereumPlugin.autoImport._
import com.mchange.sc.v1.sbtethereum.api.Formatting._

import com.mchange.sc.v1.consuela.ethereum.{EthAddress,stub}
import com.mchange.sc.v1.consuela.ethereum.stub.sol

import com.mchange.sc.v1.ethquip.contract

object QuipPlugin extends AutoPlugin {
  object autoImport {
    val quipContractAddress = settingKey[String]("Address of the Quip contract (expected under the current session Chain ID and Node URL).")

    val quipAdd  = inputKey[Unit]("Adds a quip to the quip list.")
    val quipList = taskKey[Unit]("Lists all the quips, with human-friendly indices.")
    val quipVote = inputKey[Unit]("Votes (as current sender) for one of the quips.")
  }
  import autoImport._

  lazy val defaults : Seq[sbt.Def.Setting[_]] = Seq(
    Compile / quipList := { quipListTask( Compile ).value },
  )

  private def quipAddTask( config : Configuration ) : Initialize[InputTask[Unit]] = Def.inputTask {
    val log = streams.value.log
    val contractAddress = EthAddress(quipContractAddress.value)

    val quip = StringBasic.parsed

    implicit val ( sctx, ssender ) = ( config / xethStubEnvironment ).value

    val Quip = contract.Quip( contractAddress ) // uses the stub context from the environment, rather than building one from scratch!

    Quip.txn.addQuip( quip )

    log.info( s"""The quip "${quip}" has been successfully added to the Quip contract at address '${formatHex(contractAddress)}'.""" )
  }

  private def quipListTask( config : Configuration ) : Initialize[Task[Unit]] = Def.task {
    val contractAddress = quipContractAddress.value

    implicit val ( sctx, ssender ) = ( config / xethStubEnvironment ).value

    val Quip = contract.Quip( contractAddress ) // uses the stub context from the environment, rather than building one from scratch!

    val count = Quip.view.quipCount().widen

    val rows = for( i <- BigInt(0) until count ) yield {
      val ( quip, quipper ) = Quip.view.getQuip(sol.UInt256(i))
      ( i+1, quip, quipper )
    }

    System.out.synchronized {
      rows.foreach { case ( humanIndex, quip, _ ) =>
        println( s"${humanIndex}. quip" )
      }
    }
  }

  private def quipVoteTask( config : Configuration ) : Initialize[InputTask[Unit]] = Def.inputTask {
    val log = streams.value.log
    val contractAddress = EthAddress(quipContractAddress.value)

    val humanIndex = NatBasic.parsed

    implicit val ( sctx, ssender ) = ( config / xethStubEnvironment ).value

    val Quip = contract.Quip( contractAddress ) // uses the stub context from the environment, rather than building one from scratch!

    val stubIndex = sol.UInt256(humanIndex - 1)

    Quip.txn.vote( stubIndex )

    val ( quip, _ ) = Quip.view.getQuip( stubIndex )

    log.info( s"""Sender '${formatHex(ssender.address)}' has voted for quip "${quip}" on the Quip contract at address '${formatHex(contractAddress)}'.""" )
  }

  // plug-in setup

  override def requires = JvmPlugin && SbtEthereumPlugin

  override def trigger = allRequirements

  override def projectSettings = defaults
}
