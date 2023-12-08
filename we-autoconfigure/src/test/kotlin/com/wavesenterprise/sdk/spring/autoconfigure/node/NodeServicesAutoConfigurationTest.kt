package com.wavesenterprise.sdk.spring.autoconfigure.node

import com.wavesenterprise.sdk.node.client.blocking.address.AddressService
import com.wavesenterprise.sdk.node.client.blocking.alias.AliasService
import com.wavesenterprise.sdk.node.client.blocking.blocks.BlocksService
import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.client.blocking.event.BlockchainEventsService
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.node.NodeInfoService
import com.wavesenterprise.sdk.node.client.blocking.pki.PkiService
import com.wavesenterprise.sdk.node.client.blocking.privacy.PrivacyService
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
import com.wavesenterprise.sdk.node.client.blocking.util.NodeUtilsService
import com.wavesenterprise.sdk.spring.autoconfigure.node.service.NodeServicesAutoConfiguration
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class NodeServicesAutoConfigurationTest {

    private val applicationContextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(NodeServicesAutoConfiguration::class.java))

    @Test
    fun `should create beans for all node services`() {
        applicationContextRunner.withUserConfiguration(TestConfiguration::class.java)
            .run { context ->
                assertThat(context).hasSingleBean(AddressService::class.java)
                assertThat(context).hasSingleBean(AliasService::class.java)
                assertThat(context).hasSingleBean(BlockchainEventsService::class.java)
                assertThat(context).hasSingleBean(BlocksService::class.java)
                assertThat(context).hasSingleBean(ContractService::class.java)
                assertThat(context).hasSingleBean(NodeInfoService::class.java)
                assertThat(context).hasSingleBean(NodeUtilsService::class.java)
                assertThat(context).hasSingleBean(PkiService::class.java)
                assertThat(context).hasSingleBean(PrivacyService::class.java)
                assertThat(context).hasSingleBean(TxService::class.java)
            }
    }

    @Configuration
    class TestConfiguration {

        @Bean
        fun nodeBlockingServiceFactory(): NodeBlockingServiceFactory = mockk<NodeBlockingServiceFactory>().also {
            every { it.addressService() } returns mockk()
            every { it.aliasService() } returns mockk()
            every { it.blockchainEventsService() } returns mockk()
            every { it.blocksService() } returns mockk()
            every { it.contractService() } returns mockk()
            every { it.nodeInfoService() } returns mockk()
            every { it.nodeUtilsService() } returns mockk()
            every { it.pkiService() } returns mockk()
            every { it.privacyService() } returns mockk()
            every { it.txService() } returns mockk()
        }
    }
}
