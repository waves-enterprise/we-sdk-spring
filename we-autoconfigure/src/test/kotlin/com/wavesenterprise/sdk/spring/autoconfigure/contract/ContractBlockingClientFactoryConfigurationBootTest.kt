package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.contract.client.invocation.factory.ContractBlockingClientFactory
import com.wavesenterprise.sdk.node.client.blocking.address.AddressService
import com.wavesenterprise.sdk.node.client.blocking.alias.AliasService
import com.wavesenterprise.sdk.node.client.blocking.blocks.BlocksService
import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.node.NodeInfoService
import com.wavesenterprise.sdk.node.client.blocking.pki.PkiService
import com.wavesenterprise.sdk.node.client.blocking.privacy.PrivacyService
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
import com.wavesenterprise.sdk.node.client.blocking.util.NodeUtilsService
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import com.wavesenterprise.sdk.node.domain.DataSize
import com.wavesenterprise.sdk.node.domain.DataValue
import com.wavesenterprise.sdk.node.domain.Fee
import com.wavesenterprise.sdk.node.domain.TxCount
import com.wavesenterprise.sdk.node.domain.TxVersion
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractImage
import com.wavesenterprise.sdk.node.domain.contract.ContractImageHash
import com.wavesenterprise.sdk.node.domain.contract.ContractName
import com.wavesenterprise.sdk.node.domain.sign.CallContractSignRequest
import com.wavesenterprise.sdk.node.domain.sign.CreateContractSignRequest
import com.wavesenterprise.sdk.node.domain.tx.UtxSize
import com.wavesenterprise.sdk.node.test.data.TestDataFactory
import com.wavesenterprise.sdk.node.test.data.TestDataFactory.Companion.callContractTx
import com.wavesenterprise.sdk.spring.autoconfigure.TestApplication
import com.wavesenterprise.sdk.spring.autoconfigure.contract.annotation.Contract
import com.wavesenterprise.sdk.spring.autoconfigure.contract.annotation.EnableContracts
import com.wavesenterprise.sdk.spring.autoconfigure.contractInfo
import com.wavesenterprise.sdk.tx.signer.TxSigner
import com.wavesenterprise.sdk.tx.signer.node.factory.TxServiceTxSignerFactory
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import java.util.Optional

@SpringBootTest(classes = [TestApplication::class])
@ExtendWith(MockKExtension::class)
@ActiveProfiles("test")
@ContextConfiguration(
    classes = [
        ContractAutoConfiguration::class,
        ContractBlockingClientFactoryConfigurationBootTest.TestConfig::class,
    ]
)
class ContractBlockingClientFactoryConfigurationBootTest {

    @Autowired
    lateinit var testOne: ContractBlockingClientFactory<TestContractOne>

    @Autowired
    lateinit var testTwo: ContractBlockingClientFactory<TestContractTwo>

    @Autowired
    lateinit var txService: TxService

    @Autowired
    lateinit var txSigner: TxSigner

    @Autowired
    lateinit var contractService: ContractService

    @BeforeEach
    fun init() {
        every { contractService.getContractInfo(any()) } returns Optional.of(contractInfo())
        every { txService.broadcast(any()) } returns callContractTx()
        every { txService.utxSize() } returns UtxSize(TxCount(10), DataSize(1))
    }

    @Test
    fun `should create and use two different contract clients`() {
        val signRequestCapture = slot<CreateContractSignRequest>()
        every { txSigner.sign(capture(signRequestCapture)) } returns TestDataFactory.createContractTx()

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

    @Test
    fun `should use contract version customizer`() {
        val expectedContractId = "DP5MggKC8GJuLZshCVNSYwBtE6WTRtMM1YPPdcmwbuNg"
        val signRequestCapture = slot<CallContractSignRequest>()
        val contractInfo = contractInfo(version = 10)
        every { txSigner.sign(capture(signRequestCapture)) } returns callContractTx()
        every {
            contractService.getContractInfo(ContractId.fromBase58(expectedContractId))
        } returns Optional.of(contractInfo)

        testOne.executeContract { contract ->
            contract.call("test")
        }
        signRequestCapture.captured.apply {
            assertThat(this.contractVersion).isEqualTo(contractInfo.version)
        }
    }

    @ContextConfiguration
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
        private val addressService: AddressService = mockk()
        private val aliasService: AliasService = mockk()
        private val blocksService: BlocksService = mockk()
        private val contractService: ContractService = mockk()
        private val nodeInfoService: NodeInfoService = mockk()
        private val nodeUtilsService: NodeUtilsService = mockk()
        private val pkiService: PkiService = mockk()
        private val privacyService: PrivacyService = mockk()
        private val txService: TxService = mockk()
        private val txSigner: TxSigner = mockk()
        private val txSignerFactory: TxServiceTxSignerFactory = mockk()

        @Bean
        fun addressService(): AddressService = addressService

        @Bean
        fun aliasService(): AliasService = aliasService

        @Bean
        fun blocksService(): BlocksService = blocksService

        @Bean
        fun contractService(): ContractService = contractService

        @Bean
        fun nodeInfoService(): NodeInfoService = nodeInfoService

        @Bean
        fun nodeUtilsService(): NodeUtilsService = nodeUtilsService

        @Bean
        fun pkiService(): PkiService = pkiService

        @Bean
        fun privacyService(): PrivacyService = privacyService

        @Bean
        fun txService(): TxService = txService

        @Bean
        fun txSigner(): TxSigner = txSigner

        @Bean
        fun txSignerFactory(): TxServiceTxSignerFactory = txSignerFactory

        @Bean
        fun nodeBlockingServiceFactory(): NodeBlockingServiceFactory = mockk<NodeBlockingServiceFactory>().also {
            every { it.addressService() } returns addressService
            every { it.aliasService() } returns aliasService
            every { it.blocksService() } returns blocksService
            every { it.contractService() } returns contractService
            every { it.nodeInfoService() } returns nodeInfoService
            every { it.nodeUtilsService() } returns nodeUtilsService
            every { it.pkiService() } returns pkiService
            every { it.privacyService() } returns privacyService
            every { it.txService() } returns txService
        }
    }

    companion object {
        val expectedContractOneCreateSignRequest = CreateContractSignRequest(
            contractName = ContractName("test-one"),
            params = listOf(
                DataEntry(
                    key = DataKey(
                        value = "action",
                    ),
                    value = DataValue.StringDataValue(value = "init")
                ),
                DataEntry(
                    key = DataKey(value = "init"),
                    value = DataValue.StringDataValue(value = "test"),
                )
            ),
            version = TxVersion(3),
            fee = Fee(0),
            image = ContractImage("test-one"),
            imageHash = ContractImageHash("test-one-image-hash"),
            senderAddress = Address("".toByteArray()),
        )

        val expectedContractTwoCreateSignRequest = CreateContractSignRequest(
            contractName = ContractName("test-two"),
            params = listOf(
                DataEntry(
                    key = DataKey(
                        value = "action",
                    ),
                    value = DataValue.StringDataValue(value = "init"),
                ),
                DataEntry(
                    key = DataKey(value = "init"),
                    value = DataValue.StringDataValue(value = "test"),
                ),
            ),
            version = TxVersion(3),
            fee = Fee(0),
            image = ContractImage("test-two"),
            imageHash = ContractImageHash("test-two-image-hash"),
            senderAddress = Address("".toByteArray()),
        )
    }
}
