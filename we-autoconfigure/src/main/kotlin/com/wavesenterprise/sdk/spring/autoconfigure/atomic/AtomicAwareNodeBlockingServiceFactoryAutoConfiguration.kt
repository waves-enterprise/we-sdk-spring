package com.wavesenterprise.sdk.spring.autoconfigure.atomic

import com.wavesenterprise.sdk.atomic.AtomicAwareContextManager
import com.wavesenterprise.sdk.atomic.AtomicAwareNodeBlockingServiceFactory
import com.wavesenterprise.sdk.atomic.AtomicBroadcaster
import com.wavesenterprise.sdk.atomic.ThreadLocalAtomicAwareContextManager
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
    fun atomicAwareContextManager(): AtomicAwareContextManager =
        ThreadLocalAtomicAwareContextManager()

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
    ): AtomicAwareNodeBlockingServiceFactoryPostProcessor =
        AtomicAwareNodeBlockingServiceFactoryPostProcessor(applicationContext)
}
