package net.corda.samples.example.contracts

import net.corda.core.identity.CordaX500Name
import net.corda.samples.example.states.AccountState
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class AccountContractTests {

    private val ledgerServices = MockServices()
    private val accountHolder = TestIdentity(CordaX500Name("MegaCorp", "London", "GB"))
    private val bank = TestIdentity(CordaX500Name("MiniCorp", "New York", "US"))
    private val balance = 1000.toDouble()

    @Test
    fun `transaction must include Create command`() {
        ledgerServices.ledger {
            transaction {
                output(AccountContract.ID, AccountState(balance, bank.party, accountHolder.party))
                fails()
                command(listOf(accountHolder.publicKey, bank.publicKey), AccountContract.Commands.Create())
                verifies()
            }
        }
    }


    @Test
    fun `transaction must have one output`() {
        ledgerServices.ledger {
            transaction {
                input(AccountContract.ID, AccountState(1000.toDouble(), bank.party, accountHolder.party))
                output(AccountContract.ID, AccountState(balance, bank.party, accountHolder.party))
                output(AccountContract.ID, AccountState(balance, bank.party, accountHolder.party))
                command(listOf(accountHolder.publicKey, bank.publicKey), AccountContract.Commands.Deposit())
                `fails with`("Only one output state should be created.")
            }
        }
    }

    @Test
    fun `accountHolder must sign deposit transaction`() {
        ledgerServices.ledger {
            transaction {
                input(AccountContract.ID, AccountState(1000.toDouble(), bank.party, accountHolder.party))
                output(AccountContract.ID, AccountState(balance, bank.party, accountHolder.party))
                command(bank.publicKey, AccountContract.Commands.Deposit())
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `bank must sign deposit transaction`() {
        ledgerServices.ledger {
            transaction {
                input(AccountContract.ID, AccountState(1000.toDouble(), bank.party, accountHolder.party))
                output(AccountContract.ID, AccountState(balance, bank.party, accountHolder.party))
                command(accountHolder.publicKey, AccountContract.Commands.Deposit())
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `bank must sign withdrawal transaction`() {
        ledgerServices.ledger {
            transaction {
                input(AccountContract.ID, AccountState(1000.toDouble(), bank.party, accountHolder.party))
                output(AccountContract.ID, AccountState(balance, bank.party, accountHolder.party))
                command(accountHolder.publicKey, AccountContract.Commands.Withdraw())
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `accountHolder must sign withdrawal transaction`() {
        ledgerServices.ledger {
            transaction {
                input(AccountContract.ID, AccountState(1000.toDouble(), bank.party, accountHolder.party))
                output(AccountContract.ID, AccountState(balance, bank.party, accountHolder.party))
                command(bank.publicKey, AccountContract.Commands.Withdraw())
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `minimum amount that can be deposited in the account 500`() {

        ledgerServices.ledger {
            transaction {
                input(AccountContract.ID, AccountState(1000.toDouble(), bank.party, accountHolder.party))
                output(AccountContract.ID, AccountState((1400).toDouble(), bank.party, accountHolder.party))
                command(listOf(accountHolder.publicKey, bank.publicKey), AccountContract.Commands.Deposit())
                `fails with`("Minimum amount that can be deposited in the account £500")
            }
        }
    }

    @Test
    fun `deposit amount provided should not be negative`() {

        ledgerServices.ledger {
            transaction {
                input(AccountContract.ID, AccountState(1000.toDouble(), bank.party, accountHolder.party))
                output(AccountContract.ID, AccountState((500).toDouble(), bank.party, accountHolder.party))
                command(listOf(accountHolder.publicKey, bank.publicKey), AccountContract.Commands.Deposit())
                `fails with`("Deposit amount provided should not be negative")
            }
        }
    }

    @Test
    fun `minimum Balance in the account after withdrawal should be 100`() {

        ledgerServices.ledger {
            transaction {
                input(AccountContract.ID, AccountState(1000.toDouble(), bank.party, accountHolder.party))
                output(AccountContract.ID, AccountState((90).toDouble(), bank.party, accountHolder.party))
                command(listOf(accountHolder.publicKey, bank.publicKey), AccountContract.Commands.Withdraw())
                `fails with`("Minimum Balance in the account after withdrawal should be £100")
            }
        }
    }

    @Test
    fun `the withdrawal amount cannot be greater than the balance`() {

        ledgerServices.ledger {
            transaction {
                input(AccountContract.ID, AccountState(1000.toDouble(), bank.party, accountHolder.party))
                output(AccountContract.ID, AccountState((-1).toDouble(), bank.party, accountHolder.party))
                command(listOf(accountHolder.publicKey, bank.publicKey), AccountContract.Commands.Withdraw())
                `fails with`("the withdrawal amount cannot be greater than the balance")
            }
        }
    }
}