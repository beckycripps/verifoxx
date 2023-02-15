package net.corda.samples.example

import net.corda.core.utilities.getOrThrow
import net.corda.samples.example.flows.AccountFlow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import kotlin.test.assertFailsWith

class AccountFlowTests {

    @Mock
    private lateinit var network: MockNetwork
    private lateinit var a: StartedMockNode
    private lateinit var b: StartedMockNode


    @Before
    fun setup() {
        network = MockNetwork(
            MockNetworkParameters(
                cordappsForAllNodes = listOf(
                    TestCordapp.findCordapp("net.corda.samples.example.contracts"),
                    TestCordapp.findCordapp("net.corda.samples.example.flows")
                )
            )
        )
        a = network.createPartyNode()
        b = network.createPartyNode()
        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        listOf(a, b).forEach { it.registerInitiatedFlow(AccountFlow.Acceptor::class.java) }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `flow rejects negative amounts`() {

        val flow = AccountFlow.Initiator(true, (-500).toDouble(), b.info.singleIdentity())
        val future = a.startFlow(flow)
        network.runNetwork()

        // The  specifies that amount cannot have negative values.
        assertFailsWith<IllegalArgumentException> { future.getOrThrow() }
    }


}