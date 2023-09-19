package com.wavesenterprise.sdk.spring.autoconfigure.contract.validation

import com.wavesenterprise.sdk.client.local.validator.LocalContractValidation
import com.wavesenterprise.sdk.spring.autoconfigure.contract.properties.ContractsProperties
import org.springframework.context.ApplicationContext

class ApplicationContextAwareContractValidation(
    private val applicationContext: ApplicationContext,
    private val contractKey: String,
) : LocalContractValidation {

    private val contractsConfigurationProperties: ContractsProperties by lazy {
        applicationContext.getBean(ContractsProperties::class.java)
    }
    override fun isEnabled(): Boolean =
        requireNotNull(contractsConfigurationProperties.config[contractKey]) {
            "Couldn't find contract config for contract with name = '$contractKey'"
        }.validationEnabled
}
