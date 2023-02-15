package net.corda.samples.example.states

import net.corda.core.contracts.*
import net.corda.samples.example.contracts.AccountContract
import net.corda.samples.example.schema.AccountSchemaV1
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * The state object recording account agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 *
 * @param balance the value of the  account.
 * @param bank the party holding the account and receiving and approving the transaction
 * @param accountHolder the account holder.
 */
@BelongsToContract(AccountContract::class)
data class AccountState(
    val balance: Double,
    val bank: Party,
    val accountHolder: Party,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) :
    LinearState, QueryableState {

    /** The public keys of the involved parties. */
    override val participants: List<AbstractParty> get() = listOf(bank, accountHolder)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is AccountSchemaV1 -> AccountSchemaV1.PersistentAccount(
                this.bank.name.toString(),
                this.accountHolder.name.toString(),
                this.balance,
                this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(AccountSchemaV1)
}
