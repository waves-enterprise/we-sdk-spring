package com.wavesenterprise.sdk.spring.autoconfigure.contract

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "contracts")
@ConstructorBinding
data class ContractsConfigurationProperties(
    val config: Map<String, ContractProps> = mutableMapOf(),
) {

    class ContractProps(
        val contractId: String? = null,
        val fee: Long? = 0,
        val version: Int? = null,
        val image: String? = null,
        val imageHash: String? = null,
        val autoUpdate: Boolean = false,
    )
}
