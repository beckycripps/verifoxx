package net.corda.samples.example.contracts

import net.corda.samples.example.states.AccountState
import net.corda.testing.node.MockServices
import org.junit.Test

class AccountStateTests {

    private val ledgerServices = MockServices()

    @Test
    @Throws(NoSuchFieldException::class)
    fun hasAmountFieldOfCorrectType() {
        // Does the balance field exist?
        AccountState::class.java.getDeclaredField("balance")
        assert(AccountState::class.java.getDeclaredField("balance").type == Double::class.javaPrimitiveType)
    }
}