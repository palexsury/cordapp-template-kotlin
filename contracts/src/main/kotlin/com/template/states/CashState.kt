package com.template.states;

import com.template.contracts.CashContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

/*@BelongsToContract(CashContract::class)
class CashState(var value: Int,
                var owner: Party,
                override val participants: List<AbstractParty> = listOf(owner)
) : ContractState*/

@BelongsToContract(CashContract::class)
class CashState (val value: Int,
                 val owner: Party) : ContractState {
    override val participants: List<AbstractParty> get() = listOf(owner)

    override fun toString(): String {
        return "value: $value" +
                "owner: $owner"
    }
}