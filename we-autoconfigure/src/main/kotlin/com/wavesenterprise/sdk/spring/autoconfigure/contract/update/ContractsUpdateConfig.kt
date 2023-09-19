package com.wavesenterprise.sdk.spring.autoconfigure.contract.update

import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.client.blocking.credentials.NodeCredentialsProvider
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
import com.wavesenterprise.sdk.spring.autoconfigure.contract.properties.ContractsProperties
import com.wavesenterprise.sdk.tx.signer.node.factory.TxServiceTxSignerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ContractsUpdateConfig {

    @Bean
    @ConditionalOnMissingBean
    fun propertyContractIdProvider(
        contractsConfigurationProperties: ContractsProperties,
    ): ContractIdProvider = PropertyContractIdProvider(
        contractsProperties = contractsConfigurationProperties,
    )

    @Bean
    fun contractsUpdateHandler(
        contractsProperties: ContractsProperties,
        contractIdProvider: ContractIdProvider,
        nodeCredentialsProvider: NodeCredentialsProvider,
        txSignerFactory: TxServiceTxSignerFactory,
        txService: TxService,
        contractService: ContractService,
    ): ContractsUpdateHandler =
        ContractsUpdateHandler(
            contractsProperties = contractsProperties,
            contractIdProvider = contractIdProvider,
            nodeCredentialsProvider = nodeCredentialsProvider,
            txSignerFactory = txSignerFactory,
            txService = txService,
            contractService = contractService,
        )

    @Bean
    fun contractUpdateListener(
        contractsUpdateHandler: ContractsUpdateHandler,
    ) = ContractsUpdateListener(
        contractsUpdateHandler = contractsUpdateHandler,
    )
}
