package com.wavesenterprise.sdk.spring.autoconfigure.node.legacy

import com.wavesenterprise.sdk.node.client.blocking.credentials.DefaultNodeCredentialsProvider
import com.wavesenterprise.sdk.node.client.blocking.credentials.NodeCredentialsProvider
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.Password
import com.wavesenterprise.sdk.spring.autoconfigure.node.properties.NodeProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(value = ["node.legacy-mode"], havingValue = "true", matchIfMissing = false)
class LegacyNodeConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "node")
    fun legacyNodeConfigurationProperties() = LegacyNodeConfigurationProperties()

    @Bean
    fun nodeCredentialsProvider(
        legacyNodeConfigurationProperties: LegacyNodeConfigurationProperties,
    ): NodeCredentialsProvider =
        DefaultNodeCredentialsProvider(
            credentialsMap = legacyNodeConfigurationProperties.getConfigForUsage()
                .filter { it.value.nodeOwnerAddress != null }
                .map { entry ->
                    Address.fromBase58(entry.value.nodeOwnerAddress!!) to
                        entry.value.keyStorePassword?.let { Password(it) }
                }.toMap()
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
                    xApiKey = legacyConfig.xApiKey,
                    xPrivacyApiKey = legacyConfig.xPrivacyApiKey,
                    feign = NodeProperties.NodeConfig.Http.Feign(
                        decode404 = legacyConfig.http.decode404,
                        connectTimeout = legacyConfig.http.connectTimeout.toLong(),
                        readTimeout = legacyConfig.http.readTimeout.toLong(),
                        loggerLevel = legacyConfig.http.loggerLevel,
                    )
                ),
                grpc = legacyConfig.grpc?.let {
                    NodeProperties.NodeConfig.Grpc(
                        address = it.address,
                        port = it.port,
                        keepAliveTime = it.keepAliveTime,
                        keepAliveWithoutCalls = it.keepAliveWithoutCalls,
                    )
                }
            )
        }.toMap(destination = mutableMapOf())
    )
}
