package com.wavesenterprise.sdk.spring.autoconfigure.contract.customizer

import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractInfo
import com.wavesenterprise.sdk.node.domain.contract.ContractVersion
import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilder
import com.wavesenterprise.sdk.spring.autoconfigure.contract.ContractsProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class ContractSignRequestContractVersionCustomizerTest {

    private lateinit var contractSignRequestContractVersionCustomizer: ContractSignRequestContractVersionCustomizer
    private val contractsProperties: ContractsProperties = mockk()
    private val contractService: ContractService = mockk()

    private val contractKey: String = "contract"

    @BeforeEach
    fun setUp() {
        contractSignRequestContractVersionCustomizer = ContractSignRequestContractVersionCustomizer(
            contractsProperties = contractsProperties,
            contractService = contractService,
        )
    }

    @Test
    fun `should not customize when contract config not found`() {
        every { contractsProperties.config[contractKey] } returns null

        assertThrows<IllegalArgumentException> {
            contractSignRequestContractVersionCustomizer.customize(
                contractKey = contractKey,
                contractSignRequestBuilder = ContractSignRequestBuilder(),
            )
        }.apply {
            assertEquals(
                "Contract version cannot be customized because the configuration " +
                    "is not found for contract: $contractKey",
                message
            )
        }
    }

    @Test
    fun `should not customize when contract info not found`() {
        val contractId = "DP5MggKC8GJuLZshCVNSYwBtE6WTRtMM1YPPdcmwbuNg"
        val properties: ContractsProperties.Properties = mockk()

        every { properties.contractId } returns contractId
        every { contractsProperties.config[contractKey] } returns properties
        every { contractService.getContractInfo(ContractId.fromBase58(contractId)) } returns Optional.empty()

        assertThrows<IllegalArgumentException> {
            contractSignRequestContractVersionCustomizer.customize(
                contractKey = contractKey,
                contractSignRequestBuilder = ContractSignRequestBuilder(),
            )
        }.apply {
            assertEquals(
                "Contract version cannot be customized because contract info not found by id: $contractId",
                message
            )
        }
    }

    @Test
    fun `should not customize when contract is inactive`() {
        val contractId = "DP5MggKC8GJuLZshCVNSYwBtE6WTRtMM1YPPdcmwbuNg"
        val properties: ContractsProperties.Properties = mockk()

        every { properties.contractId } returns contractId
        every { contractsProperties.config[contractKey] } returns properties
        every { contractService.getContractInfo(ContractId.fromBase58(contractId)) } returns Optional.of(
            ContractInfo(
                id = mockk(),
                image = mockk(),
                imageHash = mockk(),
                version = mockk(),
                active = false,
            )
        )

        assertThrows<IllegalArgumentException> {
            contractSignRequestContractVersionCustomizer.customize(
                contractKey = contractKey,
                contractSignRequestBuilder = ContractSignRequestBuilder(),
            )
        }.apply {
            assertEquals(
                "Contract version cannot be customized because contract is inactive: $contractId",
                message
            )
        }
    }

    @Test
    fun `should customize contract version of ContractSignRequest`() {
        val contractId = "DP5MggKC8GJuLZshCVNSYwBtE6WTRtMM1YPPdcmwbuNg"
        val contractVersionFromNode = 1
        val properties: ContractsProperties.Properties = mockk()
        val contractSignRequestBuilder: ContractSignRequestBuilder = mockk()

        every { properties.contractId } returns contractId
        every { contractsProperties.config[contractKey] } returns properties
        every { contractService.getContractInfo(ContractId.fromBase58(contractId)) } returns Optional.of(
            ContractInfo(
                id = mockk(),
                image = mockk(),
                imageHash = mockk(),
                version = ContractVersion.fromInt(contractVersionFromNode),
                active = true,
            )
        )
        val contractVersionCaptor = slot<ContractVersion>()
        every {
            contractSignRequestBuilder.contractVersion(capture(contractVersionCaptor))
        } returns contractSignRequestBuilder

        contractSignRequestContractVersionCustomizer.customize(
            contractKey = contractKey,
            contractSignRequestBuilder = contractSignRequestBuilder,
        )

        verify { contractSignRequestBuilder.contractVersion(contractVersionCaptor.captured) }
        assertEquals(contractVersionFromNode, contractVersionCaptor.captured.value)
    }
}
