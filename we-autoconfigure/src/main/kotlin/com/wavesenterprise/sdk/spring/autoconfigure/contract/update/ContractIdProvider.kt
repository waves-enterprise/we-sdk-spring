package com.wavesenterprise.sdk.spring.autoconfigure.contract.update

import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractName

interface ContractIdProvider {
    fun getIds(contractName: ContractName): List<ContractId>
}
