package com.wavesenterprise.sdk.spring.autoconfigure.signer

import com.wavesenterprise.sdk.node.client.blocking.credentials.NodeCredentialsProvider
import com.wavesenterprise.sdk.node.client.blocking.credentials.SenderAddressProvider
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
import com.wavesenterprise.sdk.spring.autoconfigure.node.service.NodeServicesAutoConfiguration
import com.wavesenterprise.sdk.tx.signer.TxSigner
import com.wavesenterprise.sdk.tx.signer.node.TxServiceTxSigner
import com.wavesenterprise.sdk.tx.signer.node.credentials.Credentials
import com.wavesenterprise.sdk.tx.signer.node.credentials.SignCredentialsProvider
import org.springframework.boot.autoconfigure.AutoConfigureAfter
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
class WeTxServiceTxSignerAutoConfiguration {

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
