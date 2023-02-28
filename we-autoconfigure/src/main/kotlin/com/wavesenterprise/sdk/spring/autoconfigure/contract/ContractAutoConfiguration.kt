package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.spring.autoconfigure.contract.update.ContractsUpdateConfig
import com.wavesenterprise.sdk.spring.autoconfigure.node.NodeBlockingServiceFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    JacksonConverterFactoryConfiguration::class,
    ContractBlockingClientFactoryConfiguration::class,
    ContractsUpdateConfig::class,
    LegacyContractsPropertiesConfiguration::class,
    ContractPropertiesConfiguration::class,
)
@AutoConfigureAfter(NodeBlockingServiceFactoryAutoConfiguration::class)
class ContractAutoConfiguration
