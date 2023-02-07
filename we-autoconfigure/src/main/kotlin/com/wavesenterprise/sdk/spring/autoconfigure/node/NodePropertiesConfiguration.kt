package com.wavesenterprise.sdk.spring.autoconfigure.node

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    NodeConfigurationProperties::class,
)
class NodePropertiesConfiguration {

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
                    feign = NodeProperties.NodeConfig.Http.Feign(
                        decode404 = config.http.feign.decode404,
                        connectTimeout = config.http.feign.connectTimeout,
                        readTimeout = config.http.feign.readTimeout,
                    )
                ),
                grpc = NodeProperties.NodeConfig.Grpc(
                    address = config.grpc.address,
                    port = config.grpc.port,
                    keepAliveTime = config.grpc.keepAliveTime,
                    keepAliveWithoutCalls = config.grpc.keepAliveWithoutCalls,
                )
            )
        }.toMap(destination = mutableMapOf())
    )
}
