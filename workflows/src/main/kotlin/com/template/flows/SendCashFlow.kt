package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.CashContract
import com.template.states.CashState
import net.corda.core.contracts.*
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.internal.InputStreamAndHash.Companion.createInMemoryTestZip
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.io.IOException
import java.util.*

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
class SendCashFlow(private val txHash: SecureHash, private val index: Int, private val newOwner: Party) : FlowLogic<Unit>() {

    constructor(txHash: SecureHash, newOwner: Party) : this(txHash, 0, newOwner)
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
        require(index >= 0) {
            "The index must be non negative."
        }
        val criteria = QueryCriteria.VaultQueryCriteria(
                Vault.StateStatus.UNCONSUMED,
                setOf(CashState::class.java),
                listOf(StateRef(txHash, index)))
        val states = serviceHub.vaultService.queryBy(CashState::class.java, criteria).states
        require(states.toSet().size == 1) {
            "There's no specific CashState in the vault."
        }
        val inputState = states.single()
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
        val inputConstraintString = inputState.state.constraint.toString()
        logger.info("Constraint of the input's state: $inputConstraintString")
        logger.info("Carefully!!! Next step is verify. Be ready to be offended ☹")
        txBuilder.verify(serviceHub)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        val otherPartySession = initiateFlow(newOwner)
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherPartySession), CollectSignaturesFlow.tracker()))
        subFlow(FinalityFlow(fullySignedTx, otherPartySession))
    }
}