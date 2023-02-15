package net.corda.samples.example.contracts

import net.corda.core.contracts.*
import net.corda.samples.example.states.AccountState
import net.corda.core.transactions.LedgerTransaction


/**
 * A implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [AccountState], which in turn encapsulates an [AccountState].
 *
 * For a new [AccountState] to be issued onto the ledger, a transaction is required which takes:
 * - one input state.
 * - One output state: the new [AccountState].
 * - A Withdraw() command with the public keys of both the lender and the borrower.
 *- A Deposite() command with the public keys of both the lender and the borrower.
 * All contracts must sub-class the [Contract] interface.
 */
class AccountContract : Contract {

    companion object {

        @JvmStatic
        val ID = "net.corda.samples.example.contracts.AccountContract"
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        requireThat {
            "Only one output state should be created." using (tx.outputs.size == 1)
        }
        val out = tx.outputsOfType<AccountState>().single()
        when (command.value) {
            is Commands.Create -> requireThat {
                //todo
            }
            is Commands.Withdraw -> requireThat {
                "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))
                "The withdrawal amount cannot be greater than the balance." using (out.balance > 0)
                "Minimum Balance in the account after withdrawal should be £100" using (out.balance >= 100)
            }
            is Commands.Deposit -> requireThat {
                val inputBalance = tx.inputsOfType<AccountState>().single().balance
                "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))
                //   Deposit amount provided should not be negative - (already covered in flow... but let's just code it here for test purposes)
                "Deposit amount provided should not be negative" using (out.balance > inputBalance)
                "Minimum amount that can be deposited in the account £500" using (out.balance - inputBalance >= 500)

            }
        }
    }

    /**
     * This contract only implements one command, Create.
     */
    interface Commands : CommandData {

        class Create : TypeOnlyCommandData(), Commands
        class Withdraw : TypeOnlyCommandData(), Commands
        class Deposit : TypeOnlyCommandData(), Commands
    }

}
