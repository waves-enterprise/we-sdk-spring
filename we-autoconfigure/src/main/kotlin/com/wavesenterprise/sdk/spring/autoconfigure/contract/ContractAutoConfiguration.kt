package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.contract.client.invocation.factory.ContractBlockingClientFactory
import com.wavesenterprise.sdk.spring.autoconfigure.contract.customizer.DefaultCustomizersConfiguration
import com.wavesenterprise.sdk.spring.autoconfigure.contract.properties.ContractPropertiesConfiguration
import com.wavesenterprise.sdk.spring.autoconfigure.contract.properties.LegacyContractsPropertiesConfiguration
import com.wavesenterprise.sdk.spring.autoconfigure.contract.update.ContractsUpdateConfig
import com.wavesenterprise.sdk.spring.autoconfigure.node.service.NodeServicesAutoConfiguration
import com.wavesenterprise.sdk.spring.autoconfigure.signer.WeTxServiceTxSignerAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    JacksonConverterFactoryConfiguration::class,
    ContractBlockingClientFactoryConfiguration::class,
    ContractsUpdateConfig::class,
    LegacyContractsPropertiesConfiguration::class,
    ContractPropertiesConfiguration::class,
    DefaultCustomizersConfiguration::class,
)
@AutoConfigureAfter(
    NodeServicesAutoConfiguration::class,
    WeTxServiceTxSignerAutoConfiguration::class,
)
@ConditionalOnClass(
    ContractBlockingClientFactory::class,
)
class ContractAutoConfiguration
