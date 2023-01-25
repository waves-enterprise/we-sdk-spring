package com.wavesenterprise.sdk.spring.node.client

import com.wavesenterprise.sdk.node.client.feign.FeignNodeClientParams
import com.wavesenterprise.sdk.node.client.feign.factory.FeignNodeServiceFactory
import com.wavesenterprise.sdk.node.domain.blocking.cache.CachingNodeBlockingServiceFactoryBuilder
import com.wavesenterprise.sdk.node.domain.blocking.credentials.DefaultNodeCredentialsProvider
import com.wavesenterprise.sdk.node.domain.blocking.lb.LbServiceFactoryBuilder
import com.wavesenterprise.sdk.node.domain.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.domain.blocking.ratelimit.DefaultRateLimiter
import com.wavesenterprise.sdk.node.domain.blocking.ratelimit.RandomDelayRateLimitingBackOff
import com.wavesenterprise.sdk.node.domain.blocking.ratelimit.RateLimiter
import com.wavesenterprise.sdk.node.domain.blocking.ratelimit.RateLimitingBackOff
import com.wavesenterprise.sdk.node.domain.blocking.ratelimit.RateLimitingServiceFactory
import com.wavesenterprise.sdk.node.domain.blocking.ratelimit.RateLimitingStrategy
import com.wavesenterprise.sdk.node.domain.blocking.ratelimit.UtxPoolSizeLimitingStrategy
import com.wavesenterprise.sdk.node.domain.blocking.tx.TxService
import com.wavesenterprise.sdk.spring.node.client.property.CachingProperties
import com.wavesenterprise.sdk.spring.node.client.property.NodeProperties
import com.wavesenterprise.sdk.spring.node.client.property.RateLimiterProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(
    NodeProperties::class,
    CachingProperties::class,
    RateLimiterProperties::class,
)
class NodeClientConfiguration {

    @Bean
    fun nodeBlockingServiceFactory(
        nodeProperties: NodeProperties,
        cachingProperties: CachingProperties,
        rateLimiter: RateLimiter,
    ): NodeBlockingServiceFactory {
        /*
        when (nodeProperties.connectionType) {
            Grpc -> build grpc cli
            Http -> build http cli via feign
        }
         */
        val clientsWithRateLim = nodeProperties.getConfigForUsage().map { // while only feign
            it.key to with(it.value) {
                RateLimitingServiceFactory(
                    nodeBlockingServiceFactory = FeignNodeServiceFactory(
                        FeignNodeClientParams(
                            url = this.http.url,
                            decode404 = this.http.feign.decode404,
                            connectTimeout = this.http.feign.connectTimeout,
                            readTimeout = this.http.feign.readTimeout,
                        )
                    ),
                    rateLimiter = rateLimiter,
                )
            }
        }.toMap()
        val lbClient = LbServiceFactoryBuilder.builder()
            .nodeCredentialsProvider(DefaultNodeCredentialsProvider(mapOf()))
            .build(clientsWithRateLim)
        return CachingNodeBlockingServiceFactoryBuilder.builder()
            .txCacheSize(cachingProperties.txCacheSize)
            .infoCacheSize(cachingProperties.policyItemInfoCacheSize)
            .cacheDuration(cachingProperties.cacheDuration)
            .build(nodeBlockingServiceFactory = lbClient)
    }

    @Bean
    fun rateLimiter(
        rateLimiterStrategy: RateLimitingStrategy,
        rateLimiterBackOff: RateLimitingBackOff,
    ): RateLimiter {
        return DefaultRateLimiter(
            strategy = rateLimiterStrategy,
            backOff = rateLimiterBackOff,
        )
    }

    @Bean
    fun rateLimiterStrategy(
        txService: TxService,
        rateLimiterProperties: RateLimiterProperties,
    ): RateLimitingStrategy {
        return UtxPoolSizeLimitingStrategy(
            txService = txService,
            maxUtx =rateLimiterProperties.maxUtxSize,
        )
    }

    @Bean
    fun rateLimiterBackOff(
        rateLimiterProperties: RateLimiterProperties,
    ): RateLimitingBackOff {
        return RandomDelayRateLimitingBackOff(
            minWaitMs =rateLimiterProperties.backOff.minWaitMs,
            maxWaitMs =rateLimiterProperties.backOff.maxWaitMs,
            maxWaitTotalMs =rateLimiterProperties.backOff.maxWaitTotalMs,
        )
    }

    @Bean
    fun addressService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.addressService()

    @Bean
    fun blocksService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.blocksService()

    @Bean
    fun contractService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.contractService()

    @Bean
    fun nodeInfoService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.nodeInfoService()

    @Bean
    fun privacyService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.privacyService()

    @Bean
    fun txService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.txService()
}
