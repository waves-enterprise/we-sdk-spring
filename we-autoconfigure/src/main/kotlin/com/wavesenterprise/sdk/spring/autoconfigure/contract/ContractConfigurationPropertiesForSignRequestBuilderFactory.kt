package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.node.domain.Fee
import com.wavesenterprise.sdk.node.domain.Hash
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractImage
import com.wavesenterprise.sdk.node.domain.contract.ContractName
import com.wavesenterprise.sdk.node.domain.contract.ContractVersion
import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilder
import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilderFactory
import org.springframework.context.ApplicationContext

class ContractConfigurationPropertiesForSignRequestBuilderFactory(
    private val contractKey: String,
    private val applicationContext: ApplicationContext,
) : ContractSignRequestBuilderFactory {

    private val contractsConfigurationProperties: ContractsConfigurationProperties by lazy {
        applicationContext.getBean(ContractsConfigurationProperties::class.java)
    }

    override fun create(): ContractSignRequestBuilder =
        requireNotNull(contractsConfigurationProperties.config[contractKey]) {
            "Couldn't find contract config for contract with name = '$contractKey'"
        }.run {
            with(ContractSignRequestBuilder()) {
                contractName(ContractName(contractKey))
                fee(Fee(0)) // where to get fee? fee from node via customizer
                image?.let {
                    image(ContractImage(it))
                }
                contractId?.let {
                    contractId(ContractId.fromBase58(it))
                }
                version?.let {
                    contractVersion(ContractVersion(it))
                }
                imageHash?.let {
                    imageHash(Hash.fromHexString(it))
                }
                apply {
                    applicationContext
                        .getBeansOfType(ContractSignRequestCustomizer::class.java)
                        .values.forEach { contractSignRequestCustomizer ->
                            contractSignRequestCustomizer.customize(
                                contractKey = contractKey,
                                contractSignRequestBuilder = this,
                            )
                        }
                }
            }
        }
}
