package com.wavesenterprise.sdk.spring.autoconfigure.atomic

import com.wavesenterprise.sdk.atomic.AtomicAwareContextManager
import com.wavesenterprise.sdk.atomic.AtomicAwareNodeBlockingServiceFactory
import com.wavesenterprise.sdk.atomic.AtomicBroadcaster
import com.wavesenterprise.sdk.spring.autoconfigure.signer.WeTxServiceTxSignerAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@AutoConfigureAfter(
    WeTxServiceTxSignerAutoConfiguration::class,
    AtomicAwareNodeBlockingServiceFactoryAutoConfiguration::class
)
@ConditionalOnClass(
    AtomicAwareNodeBlockingServiceFactory::class,
)
class AtomicTxSignerAutoConfiguration {

    @Bean
    fun atomicAwareTxSignerPostProcessor(
        atomicAwareContextManager: AtomicAwareContextManager,
    ) = AtomicAwareTxSignerPostProcessor(atomicAwareContextManager)

    @Bean
    fun atomicAspect(atomicBroadcaster: AtomicBroadcaster) =
        AtomicAspect(atomicBroadcaster)
}
