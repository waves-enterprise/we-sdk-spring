package com.wavesenterprise.sdk.spring.autoconfigure.node.service

import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NodeServicesAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun addressService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.addressService()

    @Bean
    @ConditionalOnMissingBean
    fun aliasService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.aliasService()

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
    fun nodeUtilsService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.nodeUtilsService()

    @Bean
    @ConditionalOnMissingBean
    fun pkiService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.pkiService()

    @Bean
    @ConditionalOnMissingBean
    fun privacyService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.privacyService()

    @Bean
    @ConditionalOnMissingBean
    fun txService(nodeBlockingServiceFactory: NodeBlockingServiceFactory) =
        nodeBlockingServiceFactory.txService()
}
