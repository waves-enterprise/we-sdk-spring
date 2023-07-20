package com.wavesenterprise.sdk.spring.autoconfigure.contract.properties

import com.wavesenterprise.sdk.node.domain.Fee
import com.wavesenterprise.sdk.node.domain.TxVersion
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractImage
import com.wavesenterprise.sdk.node.domain.contract.ContractImageHash
import com.wavesenterprise.sdk.node.domain.contract.ContractName
import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilder
import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilderFactory
import com.wavesenterprise.sdk.spring.autoconfigure.contract.ContractSignRequestCustomizer
import org.springframework.context.ApplicationContext

class ContractConfigurationPropertiesForSignRequestBuilderFactory(
    private val contractKey: String,
    private val applicationContext: ApplicationContext,
) : ContractSignRequestBuilderFactory {

    private val contractsConfigurationProperties: ContractsProperties by lazy {
        applicationContext.getBean(ContractsProperties::class.java)
    }

    private val contractSignRequestCustomizers: Map<String, ContractSignRequestCustomizer> by lazy {
        applicationContext.getBeansOfType(ContractSignRequestCustomizer::class.java)
    }

    override fun create(contractId: ContractId?): ContractSignRequestBuilder =
        requireNotNull(contractsConfigurationProperties.config[contractKey]) {
            "Couldn't find contract config for contract with name = '$contractKey'"
        }.run {
            with(ContractSignRequestBuilder()) {
                contractName(ContractName(contractKey))
                fee(Fee(fee))
                image?.let {
                    image(ContractImage(it))
                }
                val actualContractId = getContractId(
                    properties = this@run,
                    contractId = contractId,
                    contractSignRequestBuilder = this,
                )
                actualContractId?.let {
                    contractId(it)
                }
                version?.let {
                    version(TxVersion(it))
                }
                imageHash?.let {
                    imageHash(ContractImageHash(it))
                }
                apply {
                    contractSignRequestCustomizers.values.forEach { contractSignRequestCustomizer ->
                        contractSignRequestCustomizer.customize(
                            contractKey = contractKey,
                            contractId = actualContractId,
                            contractSignRequestBuilder = this,
                        )
                    }
                }
            }
        }

    private fun getContractId(
        properties: ContractsProperties.Properties,
        contractId: ContractId?,
        contractSignRequestBuilder: ContractSignRequestBuilder
    ): ContractId? {
        val actualContractId = properties.contractId?.let {
            ContractId.fromBase58(it)
        } ?: contractId
        actualContractId?.let {
            contractSignRequestBuilder.contractId(it)
        }
        return actualContractId
    }
}
