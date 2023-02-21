package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam
import com.wavesenterprise.sdk.contract.client.invocation.factory.ContractBlockingClientFactory
import com.wavesenterprise.sdk.node.client.blocking.address.AddressService
import com.wavesenterprise.sdk.node.client.blocking.blocks.BlocksService
import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.node.NodeInfoService
import com.wavesenterprise.sdk.node.client.blocking.privacy.PrivacyService
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import com.wavesenterprise.sdk.node.domain.DataSize
import com.wavesenterprise.sdk.node.domain.DataValue
import com.wavesenterprise.sdk.node.domain.Fee
import com.wavesenterprise.sdk.node.domain.Hash
import com.wavesenterprise.sdk.node.domain.PublicKey
import com.wavesenterprise.sdk.node.domain.Timestamp
import com.wavesenterprise.sdk.node.domain.TxCount
import com.wavesenterprise.sdk.node.domain.TxId
import com.wavesenterprise.sdk.node.domain.TxVersion
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractImage
import com.wavesenterprise.sdk.node.domain.contract.ContractName
import com.wavesenterprise.sdk.node.domain.contract.ContractVersion
import com.wavesenterprise.sdk.node.domain.sign.CallContractSignRequest
import com.wavesenterprise.sdk.node.domain.sign.CreateContractSignRequest
import com.wavesenterprise.sdk.node.domain.tx.CallContractTx
import com.wavesenterprise.sdk.node.domain.tx.CreateContractTx
import com.wavesenterprise.sdk.node.domain.tx.UtxSize
import com.wavesenterprise.sdk.node.test.data.TestDataFactory
import com.wavesenterprise.sdk.node.test.data.Util.Companion.randomBytesFromUUID
import com.wavesenterprise.sdk.spring.autoconfigure.contract.annotation.Contract
import com.wavesenterprise.sdk.spring.autoconfigure.contract.annotation.EnableContracts
import com.wavesenterprise.sdk.tx.signer.TxSigner
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(classes = [TestApp::class])
@ExtendWith(MockKExtension::class)
@ActiveProfiles("test")
@ContextConfiguration(classes = [ContractAutoConfiguration::class, TestConfig::class])
class ContractBlockingClientFactoryConfigurationBootTest {

    @Autowired
    lateinit var testOne: ContractBlockingClientFactory<TestContractOne>

    @Autowired
    lateinit var testTwo: ContractBlockingClientFactory<TestContractTwo>

    @Autowired
    lateinit var nodeBlockingServiceFactory: NodeBlockingServiceFactory

    @Autowired
    lateinit var txService: TxService

    @Autowired
    lateinit var txSigner: TxSigner

    @BeforeEach
    fun init() {
        every { txService.broadcast(any()) } returns callContractTx()
        every { nodeBlockingServiceFactory.txService() } returns txService
        every { txService.utxInfo() } returns UtxSize(TxCount(10), DataSize(1))
        every { nodeBlockingServiceFactory.txService() } returns txService
        every { txService.broadcast(any()) } returns callContractTx()
    }

    @Test
    fun `should create and use two different contract clients`() {
        val signRequestCapture = slot<CreateContractSignRequest>()
        every { txSigner.sign(capture(signRequestCapture)) } returns createContractTx()

        testOne.executeContract { contract ->
            contract.init("test")
        }

        assertThat(signRequestCapture.captured).isEqualTo(expectedContractOneCreateSignRequest)

        testTwo.executeContract { contract ->
            contract.init("test")
        }
        assertThat(signRequestCapture.captured).isEqualTo(expectedContractTwoCreateSignRequest)
    }

    @Test
    fun `should use contract config from properties`() {
        val expectedContractId = "DP5MggKC8GJuLZshCVNSYwBtE6WTRtMM1YPPdcmwbuNg"
        val signRequestCapture = slot<CallContractSignRequest>()
        every { txSigner.sign(capture(signRequestCapture)) } returns callContractTx()

        testOne.executeContract { contract ->
            contract.call("test")
        }
        signRequestCapture.captured.apply {
            assertThat(this.contractId.asBase58String()).isEqualTo(expectedContractId)
        }
    }

    companion object {
        val expectedContractOneCreateSignRequest = CreateContractSignRequest(
            contractName = ContractName("test-one"),
            params = listOf(
                DataEntry(
                    key = DataKey(
                        value = "action"
                    ),
                    value = DataValue.StringDataValue(value = "init")
                ),
                DataEntry(
                    key = DataKey(value = "init"),
                    value = DataValue.StringDataValue(value = "test")
                )
            ),
            version = null,
            fee = Fee(0),
            image = ContractImage("test-one"),
            imageHash = Hash.fromHexString("3f4f83fc8ba870f6c5e238de1fe57eb4d6cf37db301faeda4f51b608ab231397"),
            senderAddress = Address("".toByteArray())
        )

        val expectedContractTwoCreateSignRequest = CreateContractSignRequest(
            contractName = ContractName("test-two"),
            params = listOf(
                DataEntry(
                    key = DataKey(
                        value = "action"
                    ),
                    value = DataValue.StringDataValue(value = "init")
                ),
                DataEntry(
                    key = DataKey(value = "init"),
                    value = DataValue.StringDataValue(value = "test")
                )
            ),
            version = null,
            fee = Fee(0),
            image = ContractImage("test-two"),
            imageHash = Hash.fromHexString("2ec57b19e038a47e1943178054d9561b0a31809fcfaba71dcba876c0f24dfe91"),
            senderAddress = Address("".toByteArray())
        )

        fun createContractTx(
            id: TxId = TestDataFactory.txId(),
            params: List<DataEntry> = emptyList(),
        ) = CreateContractTx(
            id = id,
            senderPublicKey = PublicKey(ByteArray(1)),
            params = params,
            fee = Fee(1L),
            version = TxVersion(1),
            proofs = null,
            timestamp = Timestamp(1L),
            feeAssetId = null,
            image = ContractImage("ContractImage"),
            imageHash = Hash(ByteArray(1)),
            contractName = ContractName("ContractName"),
            atomicBadge = null,
            senderAddress = Address(ByteArray(1)),
        )

        fun callContractTx() = CallContractTx(
            id = TxId(randomBytesFromUUID()),
            senderPublicKey = PublicKey(randomBytesFromUUID()),
            contractId = ContractId(TxId(randomBytesFromUUID())),
            params = emptyList(),
            fee = Fee(0L),
            timestamp = Timestamp(0L),
            contractVersion = ContractVersion(1),
            senderAddress = Address(randomBytesFromUUID()),
            version = TxVersion(1),
        )
    }
}

interface TestContractOne {
    @ContractInit
    fun init(@InvokeParam("init") str: String)

    @ContractAction
    fun call(@InvokeParam("call") str: String)
}

class TestContractOneImpl : TestContractOne {
    override fun init(str: String) {}
    override fun call(str: String) {}
}

interface TestContractTwo {
    @ContractInit
    fun init(@InvokeParam("init") str: String)
}

class TestContractTwoImpl : TestContractTwo {
    override fun init(str: String) {}
}

@TestConfiguration
@EnableContracts(
    contracts = [
        Contract(
            api = TestContractOne::class,
            impl = TestContractOneImpl::class,
            name = "test-one",
        ),
        Contract(
            api = TestContractTwo::class,
            impl = TestContractTwoImpl::class,
            name = "test-two",
        ),
    ]
)
class TestConfig {

    private val txService: TxService = mockk()

    @Bean
    fun txSigner(): TxSigner = mockk()

    @Bean
    fun txService(): TxService = txService

    @Bean
    fun addressService(): AddressService = mockk()

    @Bean
    fun blocksService(): BlocksService = mockk()

    @Bean
    fun contractService(): ContractService = mockk()

    @Bean
    fun nodeInfoService(): NodeInfoService = mockk()

    @Bean
    fun privacyService(): PrivacyService = mockk()

    @Bean
    fun nodeBlockingServiceFactory(): NodeBlockingServiceFactory = mockk<NodeBlockingServiceFactory>().also {
        every { it.txService() } returns txService
        every { it.addressService() } returns addressService()
        every { it.blocksService() } returns blocksService()
        every { it.contractService() } returns contractService()
        every { it.nodeInfoService() } returns nodeInfoService()
        every { it.privacyService() } returns privacyService()
    }
}

@SpringBootApplication
class TestApp
