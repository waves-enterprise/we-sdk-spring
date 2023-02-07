package com.wavesenterprise.sdk.spring.autoconfigure.node

import com.wavesenterprise.sdk.node.client.blocking.credentials.DefaultNodeCredentialsProvider
import com.wavesenterprise.sdk.node.client.blocking.credentials.NodeCredentialsProvider
import com.wavesenterprise.sdk.node.domain.Address
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    NodeCredentialsProperties::class,
)
class NodeCredentialsProviderConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun nodeCredentialsProvider(
        nodeCredentialsProperties: NodeCredentialsProperties,
    ): NodeCredentialsProvider =
        DefaultNodeCredentialsProvider(
            credentialsMap = nodeCredentialsProperties.addresses.map { (address, password) ->
                Address.fromBase58(address) to password
            }.toMap()
        )
}
