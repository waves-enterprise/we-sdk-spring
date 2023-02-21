package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.contract.core.converter.factory.ConverterFactory
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
import com.wavesenterprise.sdk.tx.signer.TxSigner
import com.wavesenterprise.sdk.tx.signer.node.factory.TxServiceTxSignerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(NodeBlockingServiceFactory::class)
class ContractBlockingClientFactoryConfiguration {

    @Bean
    fun txServiceTxSignerFactory(
        txService: TxService,
    ): TxServiceTxSignerFactory = TxServiceTxSignerFactory(
        txService = txService,
    )

    @Bean
    fun enabledContractBeanRegistryPostProcessor(
        txSigner: TxSigner?,
        nodeBlockingServiceFactory: NodeBlockingServiceFactory,
        converterFactory: ConverterFactory,
        applicationContext: ApplicationContext,
    ): ContractBlockingClientFactoryRegistryPostProcessor = ContractBlockingClientFactoryRegistryPostProcessor(
        txSigner = txSigner,
        nodeBlockingServiceFactory = nodeBlockingServiceFactory,
        converterFactory = converterFactory,
        applicationContext = applicationContext,
    )
}
