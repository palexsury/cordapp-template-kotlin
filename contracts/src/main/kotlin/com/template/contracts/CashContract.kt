package com.template.contracts

import com.template.states.CashState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.CommandWithParties
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class CashContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.CashContract"
    }

    class Create : CommandData
    class Send : CommandData
    class ConsumeAll : CommandData

    override fun verify(tx: LedgerTransaction) {
        tx.commands.forEach {
            when(it) {
                Create::class -> verifyCreateCash(tx, it)
                Send::class -> verifySendCash(tx, it)
                ConsumeAll::class -> verifyConsumeAllCash(tx, it)
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
            println("1")
            "There should be one input state of type CashState.".using(tx.inputs.size == 1)
            println("2")
            "There should be one output state of type CashState.".using(tx.outputs.size == 1)
            println("3")
            val input = tx.inputsOfType<CashState>().single()
            println("4")
            val output = tx.outputsOfType<CashState>().single()
            println("5")
            "The CashState's value must be positive.".using(input.value > 0 && output.value > 0)
            println("6")
            "The old amount of cash and the new amount of cash must be the same.".using(input.value == output.value)
            println("7")
            val oldOwner = input.owner
            println("8")
            val newOwner = output.owner
            println("9")
            "The old owner and the new owner cannot be the same entity".using(oldOwner != newOwner)
            println("10")
            "There must be two signers.".using(command.signers.toSet().size == 2)
            println("11")
            val expectedSigners = listOf(oldOwner.owningKey, newOwner.owningKey)
            println("12")
            "The old owner and the new owner must be signers".using(command.signers.containsAll(expectedSigners))
            println("13")
        }
    }

    private fun verifyConsumeAllCash(tx: LedgerTransaction, command: CommandWithParties<CommandData>) {
        requireThat {
            "No output should be released when consuming all CashStates.".using(tx.outputs.isEmpty())
            val inputs = tx.inputsOfType<CashState>()
            //val expectedSigners = listOf(output.owner.owningKey)
            "There must be one signer.".using(command.signers.toSet().size == 1)
            val expectedOwner = command.signers.single()
            "The owner must be signer.".using(command.signers.containsAll(listOf(expectedOwner)))
            "There should be at least one input state of type CashState.".using(tx.inputs.isNotEmpty())
            inputs.forEach {
                "There's a CashState doesn't match expected owner:\n" + it.toString() + "\n".using(it.owner.owningKey == expectedOwner)
            }
        }
    }

}