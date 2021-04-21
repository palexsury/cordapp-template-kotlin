package com.template.contracts

import com.template.states.CashState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.CommandWithParties
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class CashContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.CashContract"
    }

    class Create : CommandData
    class Send : CommandData

    override fun verify(tx: LedgerTransaction) {
        tx.commands.forEach {
            when(it) {
                Create::class -> verifyCreateCash(tx, it)
                Send::class -> verifySendCash(tx, it)
            }
        }
    }

    private fun verifyCreateCash(tx: LedgerTransaction, command: CommandWithParties<CommandData>) {
        requireThat {
            "No inputs should be consumed when issuing an CashState.".using(tx.inputs.isEmpty())
            "There should be one output state of type CashState.".using(tx.outputStates.size == 1)
            val output = tx.outputsOfType<CashState>().single()
            "The Cash's value must be non-negative." using (output.value > 0)
            val expectedSigners = listOf(output.owner.owningKey)
            "There must be one signer.".using(command.signers.toSet().size == 1)
            "The owner must be signer.".using(command.signers.containsAll(expectedSigners))
        }
    }

    private fun verifySendCash(tx: LedgerTransaction, command: CommandWithParties<CommandData>) {
        requireThat {
            "There should be one input state of type CashState.".using(tx.inputs.size == 1)
            "There should be one output state of type CashState.".using(tx.outputs.size == 1)
            val input = tx.inputsOfType<CashState>().single()
            val output = tx.outputsOfType<CashState>().single()
            "The CashState's value must be positive.".using(input.value > 0 && output.value > 0)
            "The old amount of cash and the new amount of cash must be the same.".using(input.value == output.value)
            val oldOwner = input.owner
            val newOwner = output.owner
            "The old owner and the new owner cannot be the same entity".using(oldOwner != newOwner)
            "There must be two signers.".using(command.signers.toSet().size == 2)
            val expectedSigners = listOf(oldOwner.owningKey, newOwner.owningKey)
            "The old owner and the new owner must be signers".using(command.signers.containsAll(expectedSigners))
        }
    }
}