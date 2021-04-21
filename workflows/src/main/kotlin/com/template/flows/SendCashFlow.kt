package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.CashContract
import com.template.states.CashState
import net.corda.core.contracts.Command
import net.corda.core.contracts.CommandData
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.internal.InputStreamAndHash.Companion.createInMemoryTestZip
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.io.IOException
import java.util.*

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
class SendCashFlow(private val newOwner: Party) : FlowLogic<Unit>() {
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
        require(serviceHub.vaultService.queryBy(CashState::class.java).states.isNotEmpty()) {
            "The cash's owner must have at least one CashState in its vault."
        }
        val inputState = serviceHub.vaultService.queryBy(CashState::class.java).states[0]
        val outputState = CashState(inputState.state.data.value, newOwner)
        val requiredSigners = listOf(ourIdentity.owningKey, newOwner.owningKey)
        val command = Command<CommandData>(CashContract.Send(), requiredSigners)
        val txBuilder = TransactionBuilder(notary)
                .addInputState(inputState)
                .addOutputState(outputState, CashContract.ID)
                .addCommand(command)
        try {
            val (inputStream) = createInMemoryTestZip(5, Byte.MAX_VALUE, "attachmentTest")
            val attachmentHash = serviceHub.attachments.importAttachment(inputStream, ourIdentity.toString(),"attachmentTest")
            txBuilder.addAttachment(attachmentHash)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        txBuilder.verify(serviceHub)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        val otherPartySession = initiateFlow(newOwner)
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherPartySession), CollectSignaturesFlow.tracker()))// Finalising the transaction.
        subFlow(FinalityFlow(fullySignedTx, otherPartySession))
    }
}