package com.wavesenterprise.sdk.spring.autoconfigure.signer

import com.wavesenterprise.sdk.node.client.blocking.credentials.DefaultSenderAddressProvider
import com.wavesenterprise.sdk.node.client.blocking.credentials.NodeCredentialsProvider
import com.wavesenterprise.sdk.node.client.blocking.credentials.SenderAddressProvider
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.Password
import com.wavesenterprise.sdk.spring.autoconfigure.contract.ContractAutoConfiguration
import com.wavesenterprise.sdk.spring.autoconfigure.node.legacy.LegacyNodeConfigurationProperties
import com.wavesenterprise.sdk.spring.autoconfigure.node.properties.NodeConfigurationProperties
import com.wavesenterprise.sdk.spring.autoconfigure.node.properties.NodeCredentialsProperties
import com.wavesenterprise.sdk.spring.autoconfigure.node.service.NodeServicesAutoConfiguration
import com.wavesenterprise.sdk.tx.signer.TxSigner
import com.wavesenterprise.sdk.tx.signer.node.TxServiceTxSigner
import com.wavesenterprise.sdk.tx.signer.node.credentials.Credentials
import com.wavesenterprise.sdk.tx.signer.node.credentials.SignCredentialsProvider
import com.wavesenterprise.sdk.tx.signer.node.factory.TxServiceTxSignerFactory
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(
    NodeBlockingServiceFactory::class,
    TxSigner::class,
)
@AutoConfigureAfter(NodeServicesAutoConfiguration::class)
@AutoConfigureBefore(ContractAutoConfiguration::class)
class WeTxServiceTxSignerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(LegacyNodeConfigurationProperties::class)
    fun defaultLegacySenderAddressProvider(
        txServiceTxSignerFactory: TxServiceTxSignerFactory,
        legacyNodeConfigurationProperties: LegacyNodeConfigurationProperties,
    ): SenderAddressProvider {
        val firstNodeCredentials = legacyNodeConfigurationProperties.getConfigForUsage()
            .filter { it.value.nodeOwnerAddress != null }
            .map { entry ->
                Address.fromBase58(entry.value.nodeOwnerAddress!!) to
                    entry.value.keyStorePassword?.let { Password(it) }
            }.toMap().entries.firstOrNull()
            ?: throw IllegalStateException("No credentials found in 'node' properties in legacy mode")
        val senderAddress = firstNodeCredentials.key
        return DefaultSenderAddressProvider(senderAddress)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(NodeConfigurationProperties::class)
    fun defaultSenderAddressProvider(
        txServiceTxSignerFactory: TxServiceTxSignerFactory,
        nodeCredentialsProperties: NodeCredentialsProperties,
    ): SenderAddressProvider {
        val firstNodeCredentials = nodeCredentialsProperties.addresses.entries.firstOrNull()
            ?: throw IllegalStateException("No credentials found in 'node' properties")
        val senderAddress = firstNodeCredentials.key
        return DefaultSenderAddressProvider(Address.fromBase58(senderAddress))
    }

    @Bean
    @ConditionalOnMissingBean
    fun txSigner(
        txService: TxService,
        senderAddressProvider: SenderAddressProvider,
        nodeCredentialsProvider: NodeCredentialsProvider,
    ): TxSigner = TxServiceTxSigner(
        txService = txService,
        signCredentialsProvider = object : SignCredentialsProvider {
            override fun credentials(): Credentials =
                senderAddressProvider.address().let { address ->
                    Credentials(
                        senderAddress = address,
                        password = nodeCredentialsProvider.getPassword(address)
                    )
                }
        }
    )
}
