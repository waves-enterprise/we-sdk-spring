package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.spring.autoconfigure.node.NodeBlockingServiceFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    JacksonConverterFactoryConfiguration::class,
    ContractBlockingClientFactoryConfiguration::class,
)
@EnableConfigurationProperties(
    ContractsConfigurationProperties::class,
)
@AutoConfigureAfter(NodeBlockingServiceFactoryAutoConfiguration::class)
class ContractAutoConfiguration
