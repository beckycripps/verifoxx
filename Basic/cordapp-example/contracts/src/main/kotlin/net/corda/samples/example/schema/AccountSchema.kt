package net.corda.samples.example.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
//4.6 changes
import org.hibernate.annotations.Type


/**
 * The family of schemas for AccountState.
 */
object AccountSchema

/**
 * An AccountState schema.
 */
object AccountSchemaV1 : MappedSchema(
    schemaFamily = AccountSchema.javaClass,
    version = 1,
    mappedTypes = listOf(PersistentAccount::class.java)
) {


    @Entity
    @Table(name = "account_states")
    class PersistentAccount(
        @Column(name = "bank")
        var bank: String,

        @Column(name = "accountHolder")
        var accountHolder: String,

        @Column(name = "balance")
        var balance: Double,

        @Column(name = "linear_id")
        @Type(type = "uuid-char")
        var linearId: UUID
    ) : PersistentState() {

        // Default constructor required by hibernate.
        constructor() : this("", "", 0.0, UUID.randomUUID())
    }
}