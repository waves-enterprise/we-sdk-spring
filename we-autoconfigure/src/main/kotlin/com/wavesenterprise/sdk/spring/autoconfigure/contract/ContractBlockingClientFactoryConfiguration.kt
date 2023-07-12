package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.contract.client.invocation.ContractLocalValidationExecutor
import com.wavesenterprise.sdk.contract.core.converter.factory.ConverterFactory
import com.wavesenterprise.sdk.contract.core.state.LocalValidationContextManager
import com.wavesenterprise.sdk.contract.core.state.ThreadLocalLocalValidationContextManager
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
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
    fun localValidationContextManager(): LocalValidationContextManager = ThreadLocalLocalValidationContextManager()

    @Bean
    fun contractLocalValidationExecutor(
        localValidationContextManager: LocalValidationContextManager,
    ): ContractLocalValidationExecutor = ContractLocalValidationExecutor(localValidationContextManager)

    @Bean
    fun enabledContractBeanRegistryPostProcessor(
        converterFactory: ConverterFactory,
        applicationContext: ApplicationContext,
        localValidationContextManager: LocalValidationContextManager,
    ): ContractBlockingClientFactoryRegistryPostProcessor = ContractBlockingClientFactoryRegistryPostProcessor(
        converterFactory = converterFactory,
        applicationContext = applicationContext,
        localValidationContextManager = localValidationContextManager,
    )
}
