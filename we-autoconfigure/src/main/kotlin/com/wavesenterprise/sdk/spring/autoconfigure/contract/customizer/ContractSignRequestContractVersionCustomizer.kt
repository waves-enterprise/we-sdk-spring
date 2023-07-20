package com.wavesenterprise.sdk.spring.autoconfigure.contract.customizer

import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilder
import com.wavesenterprise.sdk.spring.autoconfigure.contract.ContractSignRequestCustomizer

class ContractSignRequestContractVersionCustomizer(
    private val contractService: ContractService,
) : ContractSignRequestCustomizer {

    override fun customize(
        contractKey: String,
        contractId: ContractId?,
        contractSignRequestBuilder: ContractSignRequestBuilder,
    ) {
        contractId?.let {
            val contractInfo = contractService.getContractInfo(contractId).orElseThrow {
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
