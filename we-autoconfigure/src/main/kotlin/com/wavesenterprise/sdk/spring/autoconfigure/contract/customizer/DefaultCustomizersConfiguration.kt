package com.wavesenterprise.sdk.spring.autoconfigure.contract.customizer

import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.spring.autoconfigure.contract.ContractSignRequestCustomizer
import com.wavesenterprise.sdk.spring.autoconfigure.contract.ContractsProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DefaultCustomizersConfiguration {

    @Bean
    fun contractSignRequestContractVersionCustomizer(
        contractsProperties: ContractsProperties,
        contractService: ContractService,
    ): ContractSignRequestCustomizer =
        ContractSignRequestContractVersionCustomizer(
            contractsProperties = contractsProperties,
            contractService = contractService,
        )
}
