package net.corda.samples.example.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.samples.example.contracts.AccountContract
import net.corda.samples.example.flows.AccountFlow.Acceptor
import net.corda.samples.example.flows.AccountFlow.Initiator
import net.corda.samples.example.states.AccountState



/**
 * This flow allows two parties (the [Initiator] and the [Acceptor]) to come to an agreement about the value encapsulated
 * within an [AccountState].
 *
 * In our simple example, the [Acceptor] always accepts a valid IOU.
 *
 * These flows have deliberately been implemented by using only the call() method for ease of understanding. In
 * practice we would recommend splitting up the various stages of the flow into sub-routines.
 *
 * All methods called within the [FlowLogic] sub-class need to be annotated with the @Suspendable annotation.
 */
object AccountFlow {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(
        val isDeposit: Boolean,
        val amount: Double,
        val accountHolder: Party
    ) : FlowLogic<SignedTransaction>() {

        /**
         * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
         * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
         */
        companion object {

            object GENERATING_TRANSACTION : Step("Generating transaction based on new account transaction value.")
            object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
            object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
            object GATHERING_SIGS : Step("Gathering the counterparty's signature.") {

                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {

                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
            )
        }

        override val progressTracker = tracker()

        /**
         * The flow logic is encapsulated within the call() method.
         */
        @Suspendable
        override fun call(): SignedTransaction {

            // Obtain a reference from a notary we wish to use.
            /**
             *  METHOD 1: Take first notary on network, WARNING: use for test, non-prod environments, and single-notary networks only!*
             *  METHOD 2: Explicit selection of notary by CordaX500Name - argument can by coded in flow or parsed from config (Preferred)
             *
             *  * - For production you always want to use Method 2 as it guarantees the expected notary is returned.
             */
            val notary = serviceHub.networkMapCache.notaryIdentities.single() // METHOD 1
            // val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")) // METHOD 2


            if (amount < 0.0) {
                throw IllegalArgumentException("The Amount provided should not be negative")
            }

            // Stage 1.
            progressTracker.currentStep = GENERATING_TRANSACTION
            // Generate an unsigned transaction.

            val inputCriteria: QueryCriteria.LinearStateQueryCriteria = QueryCriteria.LinearStateQueryCriteria()
                .withExactParticipants(listOf(accountHolder))
            val (state, ref) = serviceHub.vaultService.queryBy(
                AccountState::class.java,
                inputCriteria
            ).states.singleOrNull() ?: throw FlowException("AccountState with id $accountHolder not found.")

            val accountState = AccountState(
                bank = serviceHub.myInfo.legalIdentities.first(),
                accountHolder = accountHolder,
                balance = 1000.00
            )
            val (newState, txCommand) = if (isDeposit) deposit(accountState) else withdrawal(accountState)
            val txBuilder = TransactionBuilder(notary)
                .addInputState(StateAndRef(state, ref))
                .addOutputState(newState, AccountContract.ID)
                .addCommand(txCommand)

            // Stage 2.
            progressTracker.currentStep = VERIFYING_TRANSACTION

            // Verify that the transaction is valid.
            txBuilder.verify(serviceHub)

            // Stage 3.
            progressTracker.currentStep = SIGNING_TRANSACTION
            // Sign the transaction.
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            // Stage 4.
            progressTracker.currentStep = GATHERING_SIGS
            // Send the state to the counterparty, and receive it back with their signature.
            val otherPartySession = initiateFlow(accountHolder)
            val fullySignedTx = subFlow(
                CollectSignaturesFlow(
                    partSignedTx,
                    setOf(otherPartySession),
                    GATHERING_SIGS.childProgressTracker()
                )
            )

            // Stage 5.
            progressTracker.currentStep = FINALISING_TRANSACTION
            // Notarise and record the transaction in both parties' vaults.
            return subFlow(
                FinalityFlow(
                    fullySignedTx,
                    setOf(otherPartySession),
                    FINALISING_TRANSACTION.childProgressTracker()
                )
            )
        }


        private fun deposit(state: AccountState): Pair<AccountState, Command<AccountContract.Commands.Deposit>> {
            if (amount < 50)
                throw IllegalArgumentException("Minimum amount to deposit in the account should be ??50")
            val accountState = AccountState(
                (state.balance + amount),
                serviceHub.myInfo.legalIdentities.first(),
                accountHolder
            )
            val txCommand = Command(AccountContract.Commands.Deposit(), accountState.participants.map { it.owningKey })
            return Pair(accountState, txCommand)
        }

        private fun withdrawal(state: AccountState): Pair<AccountState, Command<AccountContract.Commands.Withdraw>> {
            if (amount > state.balance)
                throw IllegalArgumentException("The withdrawal amount cannot be greater than the balance.")
            val accountState = AccountState(
                (state.balance - amount),
                serviceHub.myInfo.legalIdentities.first(),
                accountHolder
            )
            val txCommand = Command(AccountContract.Commands.Withdraw(), accountState.participants.map { it.owningKey })
            return Pair(accountState, txCommand)
        }
    }

    @InitiatedBy(Initiator::class)
    class Acceptor(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an Account transaction." using (output is AccountState)
                    val account = output as AccountState
                    "I won't accept account transactions with a value over 100." using (account.balance <= 100)
                }
            }
            val txId = subFlow(signTransactionFlow).id

            return subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
        }
    }
}
