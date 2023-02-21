package com.wavesenterprise.sdk.spring.autoconfigure.node

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
    NodePropertiesConfiguration::class,
    NodeCredentialsProviderConfiguration::class,
)
@ConditionalOnClass(
    NodeBlockingServiceFactory::class,
    FeignNodeServiceFactory::class,
)
class NodeBlockingServiceFactoryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun nodeBlockingServiceFactory(
        nodeProperties: NodeProperties,
        cacheProperties: CacheProperties,
        rateLimiterProperties: RateLimiterProperties,
        nodeCredentialsProvider: NodeCredentialsProvider,
    ): NodeBlockingServiceFactory {
        val rateLimiterClients: Map<String, RateLimitingServiceFactory> =
            nodeProperties.config.map { (nodeAlias, nodeConfig) ->
                nodeAlias to with(nodeConfig) {
                    val feignClient = FeignNodeServiceFactory(
                        FeignNodeClientParams(
                            url = this.http.url,
                            decode404 = this.http.feign.decode404,
                            connectTimeout = this.http.feign.connectTimeout,
                            readTimeout = this.http.feign.readTimeout,
                        )
                    )
                    val rateLimiter = DefaultRateLimiter(
                        strategy = UtxPoolSizeLimitingStrategy(
                            txService = feignClient.txService(),
                            maxUtx = rateLimiterProperties.maxUtx,
                        ),
                        backOff = RandomDelayRateLimitingBackOff(
                            minWaitMs = rateLimiterProperties.minWait.toMillis(),
                            maxWaitMs = rateLimiterProperties.maxWait.toMillis(),
                            maxWaitTotalMs = rateLimiterProperties.maxWaitTotal.toMillis(),
                        ),
                    )
                    RateLimitingServiceFactory(
                        nodeBlockingServiceFactory = feignClient,
                        rateLimiter = rateLimiter,
                    )
                }
            }.toMap()
        val lbClient: LoadBalancingServiceFactory = LbServiceFactoryBuilder.builder()
            .nodeCredentialsProvider(nodeCredentialsProvider)
            .build(rateLimiterClients)
        return CachingNodeBlockingServiceFactoryBuilder.builder()
            .txCacheSize(cacheProperties.txCacheSize)
            .infoCacheSize(cacheProperties.policyItemInfoCacheSize).cacheDuration(cacheProperties.cacheDuration)
            .build(nodeBlockingServiceFactory = lbClient)
    }

    @Bean
    @ConditionalOnMissingBean
    fun addressService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.addressService()

    @Bean
    @ConditionalOnMissingBean
    fun blocksService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.blocksService()

    @Bean
    @ConditionalOnMissingBean
    fun contractService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.contractService()

    @Bean
    @ConditionalOnMissingBean
    fun nodeInfoService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.nodeInfoService()

    @Bean
    @ConditionalOnMissingBean
    fun privacyService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.privacyService()

    @Bean
    @ConditionalOnMissingBean
    fun txService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.txService()
}
