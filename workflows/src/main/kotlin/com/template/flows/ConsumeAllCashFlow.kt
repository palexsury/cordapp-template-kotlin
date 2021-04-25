package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.CashContract
import com.template.states.CashState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.*
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class ConsumeAllCashFlow() : FlowLogic<Unit>() {
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
    override fun call() {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val inputs = serviceHub.vaultService.queryBy(CashState::class.java).states
        require(inputs.isNotEmpty()) {
            "There's no single Unconsumed CashState in the vault."
        }
        val requiredSigners = listOf(ourIdentity.owningKey)
        val command = Command(CashContract.ConsumeAll(), requiredSigners)
        val txBuilder = TransactionBuilder(notary)
                .addCommand(command)
        inputs.forEach { txBuilder.addInputState(it) }
        txBuilder.verify(serviceHub)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(FinalityFlow(signedTx, emptyList()))
    }
}