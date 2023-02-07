package com.wavesenterprise.sdk.spring.autoconfigure.node

import com.wavesenterprise.sdk.node.client.blocking.credentials.DefaultNodeCredentialsProvider
import com.wavesenterprise.sdk.node.client.blocking.credentials.NodeCredentialsProvider
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.spring.autoconfigure.node.legacy.LegacyNodeConfigurationProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    LegacyNodeConfigurationProperties::class,
)
@ConditionalOnProperty(value = ["node.legacy-mode"], havingValue = "true", matchIfMissing = false)
class LegacyNodeConfiguration {

    @Bean
    fun nodeCredentialsProvider(
        legacyNodeConfigurationProperties: LegacyNodeConfigurationProperties,
    ): NodeCredentialsProvider =
        DefaultNodeCredentialsProvider(
            credentialsMap = legacyNodeConfigurationProperties.getConfigForUsage()
                .filter { it.value.nodeOwnerAddress != null }
                .map { Address.fromBase58(it.value.nodeOwnerAddress!!) to (it.value.keyStorePassword ?: "") }.toMap()
        )

    @Bean
    fun nodeProperties(
        legacyNodeConfigurationProperties: LegacyNodeConfigurationProperties,
    ) = NodeProperties(
        validationEnabled = legacyNodeConfigurationProperties.validationEnabled,
        config = legacyNodeConfigurationProperties.getConfigForUsage().map { (nodeAlias, legacyConfig) ->
            nodeAlias to NodeProperties.NodeConfig(
                http = NodeProperties.NodeConfig.Http(
                    url = legacyConfig.http.url,
                    feign = NodeProperties.NodeConfig.Http.Feign(
                        decode404 = legacyConfig.http.decode404,
                        connectTimeout = legacyConfig.http.connectTimeout.toLong(),
                        readTimeout = legacyConfig.http.readTimeout.toLong(),
                    )
                ),
                grpc = NodeProperties.NodeConfig.Grpc(
                    address = legacyConfig.grpc.address,
                    port = legacyConfig.grpc.port,
                    keepAliveTime = legacyConfig.grpc.keepAliveTime,
                    keepAliveWithoutCalls = legacyConfig.grpc.keepAliveWithoutCalls,
                )
            )
        }.toMap(destination = mutableMapOf())
    )
}
