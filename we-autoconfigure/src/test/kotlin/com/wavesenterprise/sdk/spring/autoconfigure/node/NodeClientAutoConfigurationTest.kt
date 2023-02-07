package com.wavesenterprise.sdk.spring.autoconfigure.node

import com.wavesenterprise.sdk.node.client.blocking.address.AddressService
import com.wavesenterprise.sdk.node.client.blocking.blocks.BlocksService
import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.client.blocking.credentials.NodeCredentialsProvider
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.node.NodeInfoService
import com.wavesenterprise.sdk.node.client.blocking.privacy.PrivacyService
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
import com.wavesenterprise.sdk.node.domain.Address
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class NodeClientAutoConfigurationTest {

    private val applicationContextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(NodeBlockingServiceFactoryAutoConfiguration::class.java))

    @Test
    fun `should configuring with enabled legacy-mode`() {
        applicationContextRunner
            .withPropertyValues("node.legacy-mode=true")
            .run { context ->
                assertThat(context).hasSingleBean(NodeBlockingServiceFactory::class.java)
            }
    }

    @Test
    fun `should create bean NodeCredentialsProvider from node config when legacy-mode is enabled`() {
        val nodeOwnerAddress = "3M3ybNZvLG7o7rnM4F7ViRPnDTfVggdfmRX"
        val keyStorePassword = "test"
        applicationContextRunner
            .withPropertyValues(
                "node.legacy-mode=true",
                "node.config.test-node.http.url=localhost",
                "node.config.test-node.nodeOwnerAddress=$nodeOwnerAddress",
                "node.config.test-node.keyStorePassword=$keyStorePassword"
            )
            .run { context ->
                assertThat(context).hasSingleBean(NodeCredentialsProvider::class.java)
                val passwordFromContext = context
                    .getBean(NodeCredentialsProvider::class.java)
                    .getPassword(Address.fromBase58(nodeOwnerAddress))
                assertThat(passwordFromContext).isEqualTo(keyStorePassword)
            }
    }

    @Test
    fun `should not use config when url have DO_NOT_USE marker or blank marker with legacy-mode is enabled`() {
        applicationContextRunner
            .withPropertyValues(
                "node.legacy-mode=true",
                "node.config.test-node.http.url=DO_NOT_USE",
                "node.config.test-node1.http.url: ",
            )
            .run { context ->
                assertThat(context).hasSingleBean(NodeProperties::class.java)
                val nodeProperties = context.getBean(NodeProperties::class.java)
                assertThat(nodeProperties.config).isEmpty()
            }
    }

    @Test
    fun `should configuring with default settings`() {
        applicationContextRunner
            .run { context ->
                assertThat(context).hasSingleBean(NodeBlockingServiceFactory::class.java)
            }
    }

    @Test
    fun `should create bean NodeCredentialsProvider from node config in default mode`() {
        val nodeOwnerAddress = "3M3ybNZvLG7o7rnM4F7ViRPnDTfVggdfmRX"
        val keyStorePassword = "test"
        applicationContextRunner
            .withPropertyValues(
                "node.credentials-provider.addresses.$nodeOwnerAddress=$keyStorePassword"
            )
            .run { context ->
                assertThat(context).hasSingleBean(NodeCredentialsProvider::class.java)
                val passwordFromContext = context.getBean(NodeCredentialsProvider::class.java)
                    .getPassword(Address.fromBase58(nodeOwnerAddress))
                assertThat(passwordFromContext).isEqualTo(keyStorePassword)
            }
    }

    @Test
    fun `should not use config when url have DO_NOT_USE marker or blank marker with default mode`() {
        applicationContextRunner
            .withPropertyValues(
                "node.config.test-node.http.url:DO_NOT_USE",
                "node.config.test-node1.http.url: ",
            )
            .run { context ->
                assertThat(context).hasSingleBean(NodeProperties::class.java)
                val nodeProperties = context.getBean(NodeProperties::class.java)
                assertThat(nodeProperties.config).isEmpty()
            }
    }

    @Test
    fun `should create beans for all node services`() {
        applicationContextRunner
            .run { context ->
                assertThat(context).hasSingleBean(AddressService::class.java)
                assertThat(context).hasSingleBean(BlocksService::class.java)
                assertThat(context).hasSingleBean(ContractService::class.java)
                assertThat(context).hasSingleBean(NodeInfoService::class.java)
                assertThat(context).hasSingleBean(PrivacyService::class.java)
                assertThat(context).hasSingleBean(TxService::class.java)
            }
    }
}
