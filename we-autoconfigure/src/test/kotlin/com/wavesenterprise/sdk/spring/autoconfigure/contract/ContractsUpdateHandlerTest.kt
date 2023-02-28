package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.client.blocking.credentials.NodeCredentialsProvider
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.Fee
import com.wavesenterprise.sdk.node.domain.Password
import com.wavesenterprise.sdk.node.domain.PublicKey
import com.wavesenterprise.sdk.node.domain.Timestamp
import com.wavesenterprise.sdk.node.domain.TxId
import com.wavesenterprise.sdk.node.domain.TxVersion
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractImage
import com.wavesenterprise.sdk.node.domain.contract.ContractImageHash
import com.wavesenterprise.sdk.node.domain.contract.ContractInfo
import com.wavesenterprise.sdk.node.domain.contract.ContractName
import com.wavesenterprise.sdk.node.domain.contract.ContractVersion
import com.wavesenterprise.sdk.node.domain.sign.UpdateContractSignRequest
import com.wavesenterprise.sdk.node.domain.tx.UpdateContractTx
import com.wavesenterprise.sdk.spring.autoconfigure.contract.update.ContractIdProvider
import com.wavesenterprise.sdk.spring.autoconfigure.contract.update.ContractsUpdateHandler
import com.wavesenterprise.sdk.spring.autoconfigure.contractProps
import com.wavesenterprise.sdk.tx.signer.TxSigner
import com.wavesenterprise.sdk.tx.signer.node.factory.TxServiceTxSignerFactory
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class ContractsUpdateHandlerTest {
    private lateinit var contractsUpdateHandler: ContractsUpdateHandler
    private val contractsConfigurationProperties: ContractsProperties = mockk()
    private val contractIdProvider: ContractIdProvider = mockk()
    private val nodeCredentialsProvider: NodeCredentialsProvider = mockk()
    private val txSignerFactory: TxServiceTxSignerFactory = mockk()
    private val txService: TxService = mockk()
    private val contractService: ContractService = mockk()
    private val txSigner: TxSigner = mockk()

    @BeforeEach
    fun init() {
        every { txSignerFactory.withCredentials(any()) } returns txSigner
        contractsUpdateHandler = ContractsUpdateHandler(
            contractsProperties = contractsConfigurationProperties,
            contractIdProvider = contractIdProvider,
            nodeCredentialsProvider = nodeCredentialsProvider,
            txSignerFactory = txSignerFactory,
            txService = txService,
            contractService = contractService,
        )
    }

    @Test
    fun `shouldn't update contract when auto update is disabled`() {
        val (contractName, contractProps) = "test-one" to contractProps(
            autoUpdateIsEnabled = false,
        )
        every { contractsConfigurationProperties.config } returns mapOf(
            contractName to contractProps,
        )

        contractsUpdateHandler.handle()
        verify { txService.broadcast(any()) wasNot Called }
    }

    @Test
    fun `shouldn't update contract when image and image hash is empty or null`() {
        val (contractName, contractProps) = "test-one" to contractProps(
            autoUpdateIsEnabled = true,
            image = "",
            imageHash = null,
        )
        every { contractsConfigurationProperties.config } returns mapOf(
            contractName to contractProps,
        )
        contractsUpdateHandler.handle()
        verify { txService.broadcast(any()) wasNot Called }
    }

    @Test
    fun `shouldn't update contract when not found contract id`() {
        val (contractName, contractProps) = "test-one" to contractProps(
            autoUpdateIsEnabled = true,
        )
        every { contractsConfigurationProperties.config } returns mapOf(
            contractName to contractProps,
        )
        every { contractIdProvider.getIds(ContractName(contractName)) } returns listOf()

        contractsUpdateHandler.handle()
        verify { txService.broadcast(any()) wasNot Called }
    }

    @Test
    fun `shouldn't update contract when it is not found in the node`() {
        val (contractName, contractProps) = "test-one" to contractProps(
            autoUpdateIsEnabled = true,
        )
        every { contractsConfigurationProperties.config } returns mapOf(
            contractName to contractProps,
        )
        every { contractIdProvider.getIds(ContractName(contractName)) } returns listOf(
            ContractId.fromBase58(contractProps.contractId!!)
        )
        every { contractService.getContractInfo(any()) } returns Optional.empty()

        contractsUpdateHandler.handle()
        verify { txService.broadcast(any()) wasNot Called }
    }

    @Test
    fun `shouldn't update contract when it does not need to be updated`() {
        val (contractName, contractProps) = "test-one" to contractProps(
            autoUpdateIsEnabled = true,
        )
        every { contractsConfigurationProperties.config } returns mapOf(
            contractName to contractProps,
        )
        every { contractIdProvider.getIds(ContractName(contractName)) } returns listOf(
            ContractId.fromBase58(contractProps.contractId!!)
        )
        every {
            nodeCredentialsProvider.getPassword(
                Address.fromBase58(contractProps.autoUpdate.contractCreatorAddress!!)
            )
        } returns Password("contract-one-password")
        every { contractService.getContractInfo(any()) } returns Optional.of(
            ContractInfo(
                active = false,
                id = ContractId.fromBase58(contractProps.contractId!!),
                image = ContractImage(contractProps.image!!),
                imageHash = ContractImageHash.fromString(contractProps.imageHash!!),
                version = ContractVersion(1),
            )
        )

        contractsUpdateHandler.handle()
        verify { txService.broadcast(any()) wasNot Called }
    }

    @Test
    fun `should send update tx for update contract`() {
        val (contractName, contractProps) = "test-one" to contractProps(
            autoUpdateIsEnabled = true,
        )
        every { contractsConfigurationProperties.config } returns mapOf(
            contractName to contractProps,
        )
        every { contractIdProvider.getIds(ContractName(contractName)) } returns listOf(
            ContractId.fromBase58(contractProps.contractId!!)
        )
        val nodePassword = "password"
        every {
            nodeCredentialsProvider.getPassword(
                Address.fromBase58(contractProps.autoUpdate.contractCreatorAddress!!)
            )
        } returns Password(nodePassword)
        every { contractService.getContractInfo(any()) } returns Optional.of(
            ContractInfo(
                active = false,
                id = ContractId.fromBase58(contractProps.contractId!!),
                image = ContractImage(contractProps.image!!),
                imageHash = ContractImageHash.fromString(NEW_IMAGE_HASH),
                version = ContractVersion(1),
            )
        )
        every { txSigner.sign(any<UpdateContractSignRequest>()) } returns mockkUpdateTx
        every { txSignerFactory.withCredentials(any()) } returns txSigner
        every { txService.broadcast(any()) } returns mockkUpdateTx

        contractsUpdateHandler.handle()
        verify { txService.broadcast(any()) }
    }

    companion object {
        private const val NEW_IMAGE_HASH = "33db35acb21872b3aee9c687eb56d66624e3b66344d399e3dc6450ca8b094318"
        private val mockkUpdateTx = UpdateContractTx(
            id = TxId("".toByteArray()),
            senderPublicKey = PublicKey("".toByteArray()),
            image = ContractImage(""),
            contractId = ContractId.fromBase58(""),
            imageHash = ContractImageHash(""),
            fee = Fee(0),
            timestamp = Timestamp(0),
            senderAddress = Address.fromBase58(""),
            version = TxVersion(1),
        )
    }
}
