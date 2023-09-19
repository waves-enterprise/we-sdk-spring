package com.wavesenterprise.sdk.spring.autoconfigure.contract.update

import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractName
import com.wavesenterprise.sdk.spring.autoconfigure.contract.properties.ContractsProperties

class PropertyContractIdProvider(
    private val contractsProperties: ContractsProperties,
) : ContractIdProvider {

    override fun getIds(contractName: ContractName) =
        contractsProperties.config[contractName.value]?.contractId?.let {
            listOf(ContractId.fromBase58(it))
        } ?: listOf()
}
