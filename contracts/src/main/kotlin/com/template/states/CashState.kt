package com.template.states;

import com.template.contracts.CashContract
import com.template.schemas.CashStateSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.util.*

/*@BelongsToContract(CashContract::class)
class CashState(var value: Int,
                var owner: Party,
                override val participants: List<AbstractParty> = listOf(owner)
) : ContractState*/

@BelongsToContract(CashContract::class)
class CashState  (val value: Int,
                 val owner: Party) : ContractState, QueryableState {
    override val participants: List<AbstractParty> get() = listOf(owner)

    override fun toString(): String {
        return "value: $value" +
                "owner: $owner"
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(CashStateSchemaV1)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        if (schema is CashStateSchemaV1) {
            return CashStateSchemaV1.PersistentCashState(
                    value = this.value,
                    owner = this.owner
            )
        }
        else throw IllegalArgumentException("Unrecognised schema $schema")
    }
}