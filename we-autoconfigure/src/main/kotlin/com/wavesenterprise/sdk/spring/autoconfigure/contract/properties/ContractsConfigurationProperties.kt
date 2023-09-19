package com.wavesenterprise.sdk.spring.autoconfigure.contract.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "contracts")
@ConstructorBinding
data class ContractsConfigurationProperties(
    val config: Map<String, Properties> = mutableMapOf(),
) {

    class Properties(
        val contractId: String? = null,
        val version: Int? = null,
        val fee: Long = 0,
        val image: String? = null,
        val imageHash: String? = null,
        val autoUpdate: AutoUpdate,
        val validationEnabled: Boolean = true,
    ) {
        class AutoUpdate(
            val enabled: Boolean = false,
            val contractCreatorAddress: String? = null,
        )
    }
}
