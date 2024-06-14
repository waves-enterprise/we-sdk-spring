package com.wavesenterprise.sdk.spring.autoconfigure.node

import com.google.common.collect.ImmutableMap
import com.wavesenterprise.sdk.node.client.blocking.cache.CachingNodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.cache.CachingNodeBlockingServiceFactoryBuilder
import com.wavesenterprise.sdk.node.client.blocking.credentials.NodeCredentialsProvider
import com.wavesenterprise.sdk.node.client.blocking.lb.LbServiceFactoryBuilder
import com.wavesenterprise.sdk.node.client.blocking.lb.LoadBalancingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.ratelimit.DefaultRateLimiter
import com.wavesenterprise.sdk.node.client.blocking.ratelimit.RandomDelayRateLimitingBackOff
import com.wavesenterprise.sdk.node.client.blocking.ratelimit.RateLimitingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.ratelimit.UtxPoolSizeLimitingStrategy
import com.wavesenterprise.sdk.node.client.feign.FeignNodeClientParams
import com.wavesenterprise.sdk.node.client.feign.factory.FeignNodeServiceFactory
import com.wavesenterprise.sdk.node.client.grpc.blocking.GrpcNodeClientParams
import com.wavesenterprise.sdk.node.client.grpc.blocking.factory.GrpcNodeServiceFactoryFactory
import com.wavesenterprise.sdk.spring.autoconfigure.node.holder.DefaultNodeBlockingServiceFactoryMapHolder
import com.wavesenterprise.sdk.spring.autoconfigure.node.holder.DefaultNodeClient
import com.wavesenterprise.sdk.spring.autoconfigure.node.holder.NodeBlockingServiceFactoryMapHolder
import com.wavesenterprise.sdk.spring.autoconfigure.node.holder.NodeClient
import com.wavesenterprise.sdk.spring.autoconfigure.node.legacy.LegacyNodeConfiguration
import com.wavesenterprise.sdk.spring.autoconfigure.node.properties.CacheProperties
import com.wavesenterprise.sdk.spring.autoconfigure.node.properties.NodeConfiguration
import com.wavesenterprise.sdk.spring.autoconfigure.node.properties.NodeProperties
import com.wavesenterprise.sdk.spring.autoconfigure.node.properties.RateLimiterProperties
import com.wavesenterprise.sdk.spring.autoconfigure.node.service.NodeServicesAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@EnableConfigurationProperties(
    CacheProperties::class,
    RateLimiterProperties::class,
)
@Import(
    LegacyNodeConfiguration::class,
    NodeConfiguration::class,
)
@ConditionalOnClass(
    NodeBlockingServiceFactory::class,
    FeignNodeServiceFactory::class,
)
@AutoConfigureBefore(NodeServicesAutoConfiguration::class)
class NodeBlockingServiceFactoryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun nodeBlockingServiceFactoryHolder(
        nodeProperties: NodeProperties,
    ) = DefaultNodeBlockingServiceFactoryMapHolder(
        map = creteClients(nodeProperties),
    )

    @Bean
    @ConditionalOnMissingBean
    fun nodeBlockingServiceFactory(
        cacheProperties: CacheProperties,
        rateLimiterProperties: RateLimiterProperties,
        nodeCredentialsProvider: NodeCredentialsProvider,
        nodeBlockingServiceFactoryMapHolder: NodeBlockingServiceFactoryMapHolder,
    ): NodeBlockingServiceFactory {
        val rateLimiterClients: Map<String, NodeBlockingServiceFactory> =
            nodeBlockingServiceFactoryMapHolder.getAll().map { (nodeAlias, nodeClient) ->
                nodeAlias to if (rateLimiterProperties.enabled) {
                    configureRateLimitingWrapper(
                        nodeBlockingServiceFactory = nodeClient.getHttpClient(),
                        rateLimiterProperties = rateLimiterProperties,
                    )
                } else nodeClient.getHttpClient()
            }.toMap()
        val lbClient: LoadBalancingServiceFactory = LbServiceFactoryBuilder.builder()
            .nodeCredentialsProvider(nodeCredentialsProvider)
            .build(rateLimiterClients)
        return if (cacheProperties.enabled) {
            configureCachingWrapper(
                cacheProperties = cacheProperties,
                lbClient = lbClient,
            )
        } else lbClient
    }

    private fun creteClients(nodeProperties: NodeProperties): ImmutableMap<String, NodeClient> {
        val map = mutableMapOf<String, NodeClient>()
        nodeProperties.config.forEach { (nodeAlias, nodeConfig) ->
            map[nodeAlias] = DefaultNodeClient(
                http = FeignNodeServiceFactory(
                    FeignNodeClientParams(
                        url = nodeConfig.http.url,
                        xApiKey = nodeConfig.http.xApiKey,
                        xPrivacyApiKey = nodeConfig.http.xPrivacyApiKey,
                        decode404 = nodeConfig.http.feign.decode404,
                        connectTimeout = nodeConfig.http.feign.connectTimeout,
                        readTimeout = nodeConfig.http.feign.readTimeout,
                    )
                ),
                grpc = nodeConfig.grpc?.let {
                    GrpcNodeServiceFactoryFactory.createClient(
                        grpcProperties = GrpcNodeClientParams(
                            address = nodeConfig.grpc.address,
                            port = nodeConfig.grpc.port,
                            keepAliveTime = nodeConfig.grpc.keepAliveTime,
                            keepAliveWithoutCalls = nodeConfig.grpc.keepAliveWithoutCalls,
                        )
                    )
                }
            )
        }
        return ImmutableMap.copyOf(map)
    }

    private fun configureRateLimitingWrapper(
        nodeBlockingServiceFactory: NodeBlockingServiceFactory,
        rateLimiterProperties: RateLimiterProperties,
    ): RateLimitingServiceFactory {
        val rateLimiter = DefaultRateLimiter(
            strategy = UtxPoolSizeLimitingStrategy(
                txService = nodeBlockingServiceFactory.txService(),
                maxUtx = rateLimiterProperties.maxUtx,
            ),
            backOff = RandomDelayRateLimitingBackOff(
                minWaitMs = rateLimiterProperties.minWait.toMillis(),
                maxWaitMs = rateLimiterProperties.maxWait.toMillis(),
                maxWaitTotalMs = rateLimiterProperties.maxWaitTotal.toMillis(),
            ),
        )
        return RateLimitingServiceFactory(
            nodeBlockingServiceFactory = nodeBlockingServiceFactory,
            rateLimiter = rateLimiter,
        )
    }

    private fun configureCachingWrapper(
        cacheProperties: CacheProperties,
        lbClient: LoadBalancingServiceFactory
    ): CachingNodeBlockingServiceFactory {
        return CachingNodeBlockingServiceFactoryBuilder.builder()
            .txCacheSize(cacheProperties.txCacheSize)
            .infoCacheSize(cacheProperties.policyItemInfoCacheSize).cacheDuration(cacheProperties.cacheDuration)
            .build(nodeBlockingServiceFactory = lbClient)
    }
}
