package com.wavesenterprise.sdk.spring.autoconfigure.contract

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    LegacyContractsConfigurationProperties::class,
)
@ConditionalOnProperty(value = ["contracts.legacy-mode"], havingValue = "true", matchIfMissing = false)
class LegacyContractsPropertiesConfiguration {

    @Bean
    fun contractsProperties(
        legacyContractsConfigurationProperties: LegacyContractsConfigurationProperties,
    ): ContractsProperties = with(legacyContractsConfigurationProperties) {
        ContractsProperties(
            config = this.config.map { (contractName, config) ->
                contractName to ContractsProperties.Properties(
                    contractId = config.id,
                    fee = config.fee ?: this.fee,
                    image = config.image,
                    imageHash = config.imageHash,
                    autoUpdate = ContractsProperties.Properties.AutoUpdate(
                        enabled = config.autoUpdate,
                        contractCreatorAddress = this.sender,
                    )
                )
            }.toMap()
        )
    }
}
