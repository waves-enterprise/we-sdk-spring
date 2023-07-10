package com.wavesenterprise.sdk.spring.autoconfigure.contract.customizer

import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilder
import com.wavesenterprise.sdk.spring.autoconfigure.contract.ContractSignRequestCustomizer
import com.wavesenterprise.sdk.spring.autoconfigure.contract.properties.ContractsProperties

class ContractSignRequestContractVersionCustomizer(
    private val contractsProperties: ContractsProperties,
    private val contractService: ContractService,
) : ContractSignRequestCustomizer {

    override fun customize(contractKey: String, contractSignRequestBuilder: ContractSignRequestBuilder) {
        val config = requireNotNull(contractsProperties.config[contractKey]) {
            errorMessage("the configuration is not found for contract: $contractKey")
        }
        config.contractId?.let { contractId ->
            val contractInfo = contractService.getContractInfo(ContractId.fromBase58(contractId)).orElseThrow {
                IllegalArgumentException(
                    errorMessage("contract info not found by id: $contractId")
                )
            }
            require(contractInfo.active) {
                errorMessage("contract is inactive: $contractId")
            }
            contractSignRequestBuilder.contractVersion(contractInfo.version)
        }
    }

    private fun errorMessage(message: String): String = "$VERSION_CUSTOMIZER_FAIL_DESCRIPTION $message"

    private companion object {
        const val VERSION_CUSTOMIZER_FAIL_DESCRIPTION = "Contract version cannot be customized because"
    }
}
