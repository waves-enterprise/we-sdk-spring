package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.google.protobuf.compiler.version
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    ContractsConfigurationProperties::class,
)
class ContractPropertiesConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun contractsProperties(
        contractsConfigurationProperties: ContractsConfigurationProperties,
    ): ContractsProperties = ContractsProperties(
        config = contractsConfigurationProperties.config.map { (contractName, config) ->
            contractName to ContractsProperties.Properties(
                contractId = config.contractId,
                version = config.version,
                fee = config.fee,
                image = config.image,
                imageHash = config.imageHash,
                autoUpdate = ContractsProperties.Properties.AutoUpdate(
                    enabled = config.autoUpdate.enabled,
                    contractCreatorAddress = config.autoUpdate.contractCreatorAddress,
                )
            )
        }.toMap()
    )
}
