package com.wavesenterprise.sdk.spring.autoconfigure.atomic

import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
import com.wavesenterprise.sdk.node.domain.Password
import com.wavesenterprise.sdk.node.domain.atomic.AtomicBadge
import com.wavesenterprise.sdk.node.domain.sign.AtomicSignRequest
import com.wavesenterprise.sdk.node.domain.sign.CreateContractSignRequest
import com.wavesenterprise.sdk.node.domain.sign.SignRequest
import com.wavesenterprise.sdk.node.domain.tx.AtomicTx
import com.wavesenterprise.sdk.node.domain.tx.CreateContractTx
import com.wavesenterprise.sdk.node.domain.tx.Tx
import com.wavesenterprise.sdk.node.test.data.TestDataFactory
import com.wavesenterprise.sdk.spring.autoconfigure.atomic.annotation.Atomic
import com.wavesenterprise.sdk.tx.signer.TxSigner
import com.wavesenterprise.sdk.tx.signer.node.TxServiceTxSigner
import com.wavesenterprise.sdk.tx.signer.node.credentials.Credentials
import com.wavesenterprise.sdk.tx.signer.node.credentials.SignCredentialsProvider
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service

@SpringBootTest(
    classes = [
        AtomicAwareNodeBlockingServiceFactoryAutoConfiguration::class,
        AtomicTxSignerAutoConfiguration::class,
        AtomicTransactionTest.TestConfig::class,
        AtomicTransactionTest.TestAtomicService::class,
    ]
)
class AtomicTransactionTest {

    @Autowired
    lateinit var testAtomicService: TestAtomicService

    @Autowired
    lateinit var txService: TxService

    @Test
    fun `should invoke atomic transaction`() {
        testAtomicService.callAtomic()
        val atomicRequestCaptor = slot<AtomicTx>()
        verify { txService.sign(any<AtomicSignRequest>()) }
        verify { txService.broadcast(capture(atomicRequestCaptor)) }
        val atomicTx = atomicRequestCaptor.captured
        assertThat(atomicTx.txs).isEqualTo(txs)
    }

    @Test
    fun `should not invoke atomic transaction`() {
        testAtomicService.doNothing()
        verify { txService.broadcast(any<AtomicTx>()) wasNot Called }
    }

    @Service
    class TestAtomicService(
        nodeBlockingServiceFactory: NodeBlockingServiceFactory
    ) {
        private val txService = nodeBlockingServiceFactory.txService()

        @Atomic
        fun callAtomic() {
            txService.signAndBroadcast(TestDataFactory.createContractSignRequest())
            txService.broadcast(TestDataFactory.createContractTx())
        }

        @Atomic
        fun doNothing() {}
    }

    @Configuration
    class TestConfig {

        private val txService: TxService = mockk<TxService>().also {
            every { it.sign(any<SignRequest<Tx>>()) } returnsMany listOf(TestDataFactory.createContractTx(), atomicTx)
            every { it.signAndBroadcast(any<CreateContractSignRequest>()) } returns TestDataFactory.createContractTx()
            every { it.broadcast(any<CreateContractTx>()) } returns TestDataFactory.createContractTx()
            every { it.broadcast(any<AtomicTx>()) } returns atomicTx
        }

        @Bean
        fun txSigner(
            signCredentialsProvider: SignCredentialsProvider,
        ): TxSigner = TxServiceTxSigner(
            txService = txService,
            signCredentialsProvider = signCredentialsProvider,
        )

        @Bean
        fun signCredentialsProvider(): SignCredentialsProvider = mockk<SignCredentialsProvider>().also {
            every { it.credentials() } returns Credentials(
                senderAddress = sender,
                password = password,
            )
        }

        @Bean
        fun txService(): TxService = txService

        @Bean
        fun nodeBlockingServiceFactory(): NodeBlockingServiceFactory = mockk<NodeBlockingServiceFactory>().also {
            every { it.txService() } returns txService
        }
    }

    companion object {
        private val sender = TestDataFactory.address()
        private val password = Password("password")
        private val createContractTx1 = TestDataFactory.createContractTx(
            senderAddress = sender,
            atomicBadge = AtomicBadge(sender),
        )
        private val createContractTx2 = TestDataFactory.createContractTx(
            senderAddress = sender,
            atomicBadge = AtomicBadge(sender),
        )
        private val txs = listOf(createContractTx1, createContractTx2)
        private val atomicTx = TestDataFactory.atomicTx(
            senderAddress = sender,
            txs = txs,
        )
    }
}
