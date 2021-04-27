package com.template.schemas

import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object CashStateSchema

object CashStateSchemaV1 : MappedSchema(
        schemaFamily = CashStateSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentCashState::class.java)) {

    @Entity
    @Table(name = "cash_states")
    class PersistentCashState(
            @Column(name = "value", nullable = false)
            var value: Int,

            /** X500Name of owner party **/
            @Column(name = "owner_name", nullable = true)
            var owner: AbstractParty?

    ): PersistentState()
}
