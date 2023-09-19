package com.wavesenterprise.sdk.spring.autoconfigure.atomic

import com.wavesenterprise.sdk.atomic.AtomicAwareNodeBlockingServiceFactory
import com.wavesenterprise.sdk.atomic.AtomicBroadcaster
import com.wavesenterprise.sdk.atomic.cache.contract.info.ThreadLocalContractInfoCacheManager
import com.wavesenterprise.sdk.atomic.manager.AtomicAwareContextManager
import com.wavesenterprise.sdk.atomic.manager.AtomicAwareContextManagerHook
import com.wavesenterprise.sdk.atomic.manager.ContractInfoCacheContextManagerHook
import com.wavesenterprise.sdk.atomic.manager.ContractInfoCacheManager
import com.wavesenterprise.sdk.atomic.manager.ThreadLocalAtomicAwareContextManagerWithHook
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.spring.autoconfigure.node.NodeBlockingServiceFactoryAutoConfiguration
import com.wavesenterprise.sdk.spring.autoconfigure.node.service.NodeServicesAutoConfiguration
import com.wavesenterprise.sdk.tx.signer.TxSigner
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration
@ConditionalOnClass(
    NodeBlockingServiceFactory::class,
    AtomicAwareNodeBlockingServiceFactory::class,
)
@EnableAspectJAutoProxy
@AutoConfigureAfter(NodeBlockingServiceFactoryAutoConfiguration::class)
@AutoConfigureBefore(NodeServicesAutoConfiguration::class)
class AtomicAwareNodeBlockingServiceFactoryAutoConfiguration {

    @Bean
    fun contractInfoCacheManager(): ContractInfoCacheManager = ThreadLocalContractInfoCacheManager()

    @Bean
    fun atomicAwareContextManagerHook(
        contractInfoCacheManager: ContractInfoCacheManager
    ): AtomicAwareContextManagerHook =
        ContractInfoCacheContextManagerHook(
            contractInfoCacheManager = contractInfoCacheManager,
        )

    @Bean
    fun atomicAwareContextManager(
        atomicAwareContextManagerHook: AtomicAwareContextManagerHook,
    ): AtomicAwareContextManager =
        ThreadLocalAtomicAwareContextManagerWithHook(
            atomicAwareContextManagerHook = atomicAwareContextManagerHook,
        )

    @Bean
    fun atomicBroadcaster(
        atomicAwareContextManager: AtomicAwareContextManager,
        txSigner: TxSigner,
        atomicAwareNodeBlockingServiceFactory: NodeBlockingServiceFactory,
    ): AtomicBroadcaster =
        AtomicBroadcaster(
            atomicAwareContextManager = atomicAwareContextManager,
            txSigner = txSigner,
            atomicAwareNodeBlockingServiceFactory = atomicAwareNodeBlockingServiceFactory,
        )

    @Bean
    fun atomicAwareNodeBlockingServiceFactoryPostProcessor(
        applicationContext: ApplicationContext,
        contractInfoCacheManager: ContractInfoCacheManager,
        atomicAwareContextManager: AtomicAwareContextManager,
    ): AtomicAwareNodeBlockingServiceFactoryPostProcessor =
        AtomicAwareNodeBlockingServiceFactoryPostProcessor(
            applicationContext = applicationContext,
            contractInfoCacheManager = contractInfoCacheManager,
            atomicAwareContextManager = atomicAwareContextManager,
        )
}
