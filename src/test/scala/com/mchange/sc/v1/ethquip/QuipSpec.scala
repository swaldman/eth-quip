package com.mchange.sc.v1.ethquip

import com.mchange.sc.v1.consuela.ethereum.stub
import com.mchange.sc.v1.consuela.ethereum.stub.sol

import com.mchange.sc.v1.consuela.ethereum.jsonrpc


import org.specs2._

import contract.Testing._

import scala.concurrent._
import scala.concurrent.duration._

import scala.util.control.NonFatal

class QuipSpec extends Specification with AutoSender { def is = sequential ^ s2"""
   A Quip contact...
      ...begins with a quip count of zero.                              ${e0}
      ...after one quip is added contains one quip.                     ${e1}
      ...whose value and sender are correct                             ${e2}
      ...does not allow a Quip author to vote for its own quip          ${e3}
      ...does allow a sender other than the Quip author to vote a quip  ${e4}
      ...successfully pays out Ether to a single quipper                ${e5}
      ...successfully pays out ERC20 to a single quipper                ${e6}

"""

  val QuipContract = contract.Quip( TestSender(0).contractAddress(0) )
  val ERC20Mintable = contract.ERC20Mintable( TestSender(0).contractAddress(1) )

  val RandomSender0 = createRandomSender()
  val RandomSender1 = createRandomSender()

  val Quip0 = "This is only a quip."

  def init() : Unit = {

    ERC20Mintable.txn.mint( DefaultSender.address, sol.UInt256(1000) )( sender = DefaultSender )
    ERC20Mintable.txn.approve( QuipContract.address, sol.UInt256(100) )( sender = DefaultSender )

    awaitFundSenders( (RandomSender0, 100.ether ) :: ( RandomSender1, 100.ether ) :: Nil )
  }

  def e0 = {
    init()
    QuipContract.view.quipCount().widen == 0
  }
  def e1 = {
    QuipContract.txn.addQuip( Quip0 )( sender = RandomSender0 )
    QuipContract.view.quipCount().widen == 1
  }
  def e2 = {
    val ( quip, quipper ) = QuipContract.view.getQuip(sol.UInt256(0))
    quip == Quip0 && quipper == RandomSender0.address
  }
  def e3 = {
    try {
      QuipContract.txn.vote(sol.UInt256(0))( sender = RandomSender0 )
      false
    }
    catch {
      case NonFatal(t) => true
    }
  }
  def e4 = {
    QuipContract.txn.vote(sol.UInt256(0))( sender = RandomSender1 )
    true
  }
  def e5 = {
    val paid = sol.UInt256(100)
    val overpaid = sol.UInt256(1000)
    val before = awaitBalance( RandomSender0 )
    QuipContract.txn.payout( sol.Address.Zero, paid, payment=stub.Payment.ofWei(overpaid) )( DefaultSender )
    val after  = awaitBalance( RandomSender0 )
    ((after - before) == paid.widen) && awaitBalance( QuipContract.address ) == 0
  }
  def e6 = {
    val paid = 100
    val payerBefore = ERC20Mintable.view.balanceOf( DefaultSender.address ).widen
    QuipContract.txn.payout( ERC20Mintable.address, sol.UInt256(paid) )( DefaultSender )
    val payerAfter =  ERC20Mintable.view.balanceOf( DefaultSender.address ).widen
    (ERC20Mintable.view.balanceOf( RandomSender0.address ).widen == paid) && ((payerBefore - payerAfter) == paid)
  }
}

