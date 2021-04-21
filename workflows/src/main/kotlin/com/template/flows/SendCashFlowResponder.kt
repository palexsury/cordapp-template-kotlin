package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.states.CashState
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction

// ******************
// * Responder flow *
// ******************
@InitiatedBy(SendCashFlow::class)
class SendCashFlowResponder(private val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    @Throws(FlowException::class)
    override fun call() {
        class SignTxFlow(otherPartySession: FlowSession) : SignTransactionFlow(otherPartySession) {
            override fun checkTransaction(stx: SignedTransaction): Unit = requireThat {
                val output = stx.tx.outputs[0].data
                "This must be an CashState transaction.".using(output is CashState)
                val cashState = output as CashState
                val jarHash = stx.tx.attachments[0]
                println("Shared attachment: $jarHash")
                //getServiceHub().getAttachments().openAttachment(jarHash).openAsJAR();
                "The CashState's value can't be too high.".using(cashState.value < 100)
            }
        }
        val expectedTxId = subFlow(SignTxFlow(otherPartySession)).id
        subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId))
    }
}