package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.CashContract
import com.template.states.CashState
import net.corda.core.contracts.Command
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.*
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
class CreateCashFlow(private val value: Int) : FlowLogic<SecureHash>() {
    /**
     * The progress tracker provides checkpoints indicating the progress of
     * the flow to observers.
     */
    override val progressTracker = ProgressTracker()

    /**
     * The flow logic is encapsulated within the call() method.
     */
    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SecureHash {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val outputState = CashState(value, ourIdentity)
        val requiredSigners = listOf(ourIdentity.owningKey)
        val command = Command(CashContract.Create(), requiredSigners)
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(outputState, CashContract.ID)
                .addCommand(command)
        txBuilder.verify(serviceHub)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        return subFlow(FinalityFlow(signedTx, emptyList())).id
    }
}