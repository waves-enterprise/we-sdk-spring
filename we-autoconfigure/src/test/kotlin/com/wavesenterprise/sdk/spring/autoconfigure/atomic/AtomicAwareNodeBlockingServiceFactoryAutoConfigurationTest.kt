package com.wavesenterprise.sdk.spring.autoconfigure.atomic

import com.wavesenterprise.sdk.atomic.AtomicAwareTxSigner
import com.wavesenterprise.sdk.atomic.manager.AtomicAwareContextManagerHook
import com.wavesenterprise.sdk.atomic.manager.ContractInfoCacheManager
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.tx.signer.TxSigner
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class AtomicAwareNodeBlockingServiceFactoryAutoConfigurationTest {

    private val applicationContextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                AtomicTxSignerAutoConfiguration::class.java,
                AtomicAwareNodeBlockingServiceFactoryAutoConfiguration::class.java,
            )
        )

    @Test
    fun `should have all needed beans for atomic implementation`() {
        applicationContextRunner.withUserConfiguration(TestConfiguration::class.java).run { context ->
            assertThat(context).hasBean("atomicAwareContextManager")
            assertThat(context).hasSingleBean(NodeBlockingServiceFactory::class.java)
            assertThat(context).hasSingleBean(AtomicAwareNodeBlockingServiceFactoryPostProcessor::class.java)
            assertThat(context).hasBean("atomicBroadcaster")
            assertThat(context).hasSingleBean(AtomicAwareTxSigner::class.java)
            assertThat(context).hasBean("atomicAspect")
            assertThat(context).hasSingleBean(ContractInfoCacheManager::class.java)
            assertThat(context).hasSingleBean(AtomicAwareContextManagerHook::class.java)
        }
    }

    @Test
    fun `should failed on start up when NodeBlockingServiceFactory is not in classpath`() {
        applicationContextRunner.run { context ->
            assertThat(context).hasFailed()
        }
    }

    @Configuration
    class TestConfiguration {
        @Bean
        fun txSigner(): TxSigner = mockk()

        @Bean
        fun nodeBlockingServiceFactory(): NodeBlockingServiceFactory = mockk<NodeBlockingServiceFactory>().also {
            every { it.txService() } returns mockk()
        }
    }
}
