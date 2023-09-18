package com.wavesenterprise.sdk.spring.autoconfigure.node.properties

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(value = ["node.legacy-mode"], havingValue = "false", matchIfMissing = true)
class NodePropertiesConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "node")
    fun nodeConfigurationProperties() = NodeConfigurationProperties()

    @Bean
    @ConditionalOnMissingBean
    fun nodeProperties(
        nodeConfigurationProperties: NodeConfigurationProperties,
    ) = NodeProperties(
        validationEnabled = nodeConfigurationProperties.validationEnabled,
        config = nodeConfigurationProperties.getConfigForUsage().map { (nodeAlias, config) ->
            nodeAlias to NodeProperties.NodeConfig(
                http = NodeProperties.NodeConfig.Http(
                    url = config.http.url,
                    xApiKey = config.http.xApiKey,
                    xPrivacyApiKey = config.http.xPrivacyApiKey,
                    feign = NodeProperties.NodeConfig.Http.Feign(
                        decode404 = config.http.feign.decode404,
                        connectTimeout = config.http.feign.connectTimeout,
                        readTimeout = config.http.feign.readTimeout,
                        loggerLevel = config.http.feign.loggerLevel,
                    )
                ),
                grpc = config.grpc?.let {
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
