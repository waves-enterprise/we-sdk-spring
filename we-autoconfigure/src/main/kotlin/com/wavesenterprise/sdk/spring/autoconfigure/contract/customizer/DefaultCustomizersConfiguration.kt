package com.wavesenterprise.sdk.spring.autoconfigure.contract.customizer

import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.spring.autoconfigure.contract.ContractSignRequestCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DefaultCustomizersConfiguration {

    @Bean
    fun contractSignRequestContractVersionCustomizer(
        contractService: ContractService,
    ): ContractSignRequestCustomizer =
        ContractSignRequestContractVersionCustomizer(
            contractService = contractService,
        )
}
